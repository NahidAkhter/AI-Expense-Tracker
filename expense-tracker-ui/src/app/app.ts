import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NotificationComponent } from './components/notification/notification';
import { HeaderComponent } from "./components/header/header";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NotificationComponent, HeaderComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('expense-tracker-ui');
}
