// In-repo in-memory shim for `lmdb` used by Angular's lmdb-cache-store.
// This avoids the native `lmdb` dependency while preserving the minimal API used by the build.

const stores = global.__lmdb_shim_stores ||= new Map();

function open(options = {}) {
  const cachePath = options.path || '__default__';
  if (stores.has(cachePath)) {
    return stores.get(cachePath);
  }

  const backing = new Map();

  const db = {
    get(key) {
      return backing.get(key);
    },
    async put(key, value) {
      backing.set(key, value);
      return undefined;
    },
    doesExist(key) {
      return backing.has(key);
    },
    async close() {
      return undefined;
    },
  };

  stores.set(cachePath, db);
  return db;
}

module.exports = { open };

