import { Component, inject } from '@angular/core';
import { ExpenseService } from '../../services/expense-service';
import { NotificationService } from '../../services/notification-service';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
})
export class DashboardComponent {
  private expenseService = inject(ExpenseService);
  private notificationService = inject(NotificationService);

  summary: any = null;
  expenseCount = 0;
  averageExpense = 0;
  categoryData: any[] = [];
  recentExpenses: any[] = [];
  loading = false;

  ngOnInit() {
    this.expenseService.expenses$.subscribe(expenses => {
      this.expenseCount = expenses.length;
      this.recentExpenses = expenses
        .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
        .slice(0, 5);

      this.calculateMetrics(expenses);
    });

    this.expenseService.summary$.subscribe(summary => {
      this.summary = summary;
      this.calculateCategoryData(summary);
    });
  }

  private calculateMetrics(expenses: any[]) {
    this.averageExpense = expenses.length > 0
      ? expenses.reduce((sum, exp) => sum + exp.amount, 0) / expenses.length
      : 0;
  }

  private calculateCategoryData(summary: any) {
    if (!summary || !summary.categoryBreakdown) {
      this.categoryData = [];
      return;
    }

    const total = summary.total;
    this.categoryData = Object.entries(summary.categoryBreakdown)
      .map(([name, amount]: [string, any]) => ({
        name,
        amount,
        percentage: total > 0 ? Math.round((amount / total) * 100) : 0
      }))
      .sort((a, b) => b.amount - a.amount);
  }

  async loadSampleData() {
    this.loading = true;
    const sampleExpenses = [
      { description: 'Groceries at Whole Foods', amount: 85.50 },
      { description: 'Uber ride to work', amount: 25.75 },
      { description: 'Netflix subscription', amount: 15.99 },
      { description: 'Electricity bill', amount: 120.00 },
      { description: 'Dinner at Italian restaurant', amount: 65.80 }
    ];

    try {
      for (const expense of sampleExpenses) {
        await new Promise(resolve => setTimeout(resolve, 500)); // Delay between requests
        this.expenseService.addExpense({
          ...expense,
          date: new Date().toISOString(),
          category: 'OTHER' // Will be categorized by AI
        }).subscribe();
      }

      this.notificationService.show({
        type: 'success',
        message: 'Sample data loaded successfully!'
      });
    } catch (error) {
      this.notificationService.show({
        type: 'error',
        message: 'Failed to load sample data'
      });
    } finally {
      this.loading = false;
    }
  }
}
