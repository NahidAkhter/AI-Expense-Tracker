#!/usr/bin/env python3
"""
Wait for a TCP service to be available.
Python implementation for Docker containers.
"""

import argparse
import socket
import sys
import time
import logging
import os

def is_port_open(host: str, port: int, timeout: float = 2.0) -> bool:
    """Check if a TCP port is open and accepting connections."""
    try:
        with socket.create_connection((host, port), timeout=timeout):
            return True
    except (socket.timeout, ConnectionRefusedError, OSError):
        return False

def wait_for_port(host: str, port: int, timeout: int = 60, quiet: bool = False) -> bool:
    """Wait for a TCP port to become available."""
    start_time = time.time()

    if not quiet:
        logging.info(f"Waiting for {host}:{port} (timeout: {timeout}s)")

    while True:
        if is_port_open(host, port):
            elapsed = time.time() - start_time
            if not quiet:
                logging.info(f"{host}:{port} is available after {elapsed:.2f} seconds")
            return True

        if time.time() - start_time > timeout:
            if not quiet:
                logging.error(f"Timeout after {timeout}s waiting for {host}:{port}")
            return False

        if not quiet:
            logging.debug(f"Port {host}:{port} not ready, retrying...")

        time.sleep(2)

def main():
    parser = argparse.ArgumentParser(description="Wait for a TCP service to be available")
    parser.add_argument("host", help="Hostname or IP address to check")
    parser.add_argument("port", type=int, help="TCP port to check")
    parser.add_argument("-t", "--timeout", type=int, default=60, help="Timeout in seconds")
    parser.add_argument("-q", "--quiet", action="store_true", help="Don't output any status messages")
    parser.add_argument("--command", nargs=argparse.REMAINDER, help="Command to execute after service is available")

    args = parser.parse_args()

    # Configure logging
    log_level = logging.ERROR if args.quiet else logging.INFO
    logging.basicConfig(level=log_level, format='%(asctime)s - %(levelname)s - %(message)s')

    # Wait for the service
    success = wait_for_port(args.host, args.port, args.timeout, args.quiet)

    if success and args.command:
        if not args.quiet:
            logging.info(f"Executing command: {' '.join(args.command)}")
        try:
            sys.exit(os.system(' '.join(args.command)))
        except Exception as e:
            logging.error(f"Error executing command: {e}")
            sys.exit(1)
    elif not success:
        sys.exit(1)

if __name__ == "__main__":
    main()