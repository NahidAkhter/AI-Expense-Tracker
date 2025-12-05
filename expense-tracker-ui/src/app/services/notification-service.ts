import { Injectable, inject } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Notification {
  id: number;
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
  duration?: number;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  private nextId = 1;

  show(notification: Omit<Notification, 'id'>): void {
    const newNotification: Notification = {
      id: this.nextId++, 
      duration: 5000,
      ...notification
    };

    const current = this.notificationsSubject.value;
    this.notificationsSubject.next([...current, newNotification]);

    if (newNotification.duration) {
      setTimeout(() => {
        this.remove(newNotification.id);
      }, newNotification.duration);
    }
  }

  remove(id: number): void {
    const current = this.notificationsSubject.value;
    this.notificationsSubject.next(current.filter(n => n.id !== id));
  }

  clear(): void {
    this.notificationsSubject.next([]);
  }
}