import { Component, inject } from '@angular/core';
import { ExpenseService } from '../../services/expense-service';
import { NotificationService } from '../../services/notification-service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-insights',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './insights.html',
  styleUrls: ['./insights.scss'],
})
export class InsightsComponent {
  private expenseService = inject(ExpenseService);
  private notificationService = inject(NotificationService);

  insights: string | null = null;
  generatingInsights = false;
  expenseCount = 0;
  totalAmount = 0;
  monthlyAverage = 0;
  topCategory = '';
  categoryDistribution: any[] = [];

  ngOnInit() {
    this.expenseService.expenses$.subscribe(expenses => {
      this.expenseCount = expenses.length;
      this.calculateStatistics(expenses);
    });

    this.expenseService.summary$.subscribe(summary => {
      if (summary) {
        this.totalAmount = summary.total;
        this.calculateCategoryDistribution(summary.categoryBreakdown);
      }
    });
  }

  generateInsights() {
    this.generatingInsights = true;

    this.expenseService.getInsights().subscribe({
      next: (insights) => {
        this.insights = insights;
        this.generatingInsights = false;

        this.notificationService.show({
          type: 'success',
          message: 'AI insights generated successfully!'
        });
      },
      error: (error) => {
        this.generatingInsights = false;

        // Fallback mock insights for demo purposes
        this.insights = this.generateMockInsights();

        this.notificationService.show({
          type: 'error',
          message: 'Using demo insights (AI service unavailable)'
        });

        console.error('Failed to get AI insights:', error);
      }
    });
  }

  regenerateInsights() {
    this.insights = null;
    this.generateInsights();
  }

  copyInsights() {
    if (this.insights) {
      navigator.clipboard.writeText(this.insights).then(() => {
        this.notificationService.show({
          type: 'success',
          message: 'Insights copied to clipboard!'
        });
      }).catch(() => {
        this.notificationService.show({
          type: 'error',
          message: 'Failed to copy insights'
        });
      });
    }
  }

  shareInsights() {
    if (this.insights) {
      if (navigator.share) {
        navigator.share({
          title: 'My Expense Insights',
          text: this.insights
        }).catch(() => {
          this.fallbackShare();
        });
      } else {
        this.fallbackShare();
      }
    }
  }

  private fallbackShare() {
    this.copyInsights();
    this.notificationService.show({
      type: 'info',
      message: 'Insights copied to clipboard. You can now share them anywhere!'
    });
  }

  private calculateStatistics(expenses: any[]) {
    this.totalAmount = expenses.reduce((sum, exp) => sum + exp.amount, 0);

    // Calculate monthly average (assuming 30 days for simplicity)
    if (expenses.length > 0) {
      const firstExpense = new Date(expenses[expenses.length - 1].date);
      const lastExpense = new Date(expenses[0].date);
      const monthsDiff = (lastExpense.getTime() - firstExpense.getTime()) / (1000 * 60 * 60 * 24 * 30);
      this.monthlyAverage = monthsDiff > 0 ? this.totalAmount / Math.max(monthsDiff, 1) : this.totalAmount;
    }

    // Find top category
    const categoryTotals = expenses.reduce((acc, exp) => {
      acc[exp.category] = (acc[exp.category] || 0) + exp.amount;
      return acc;
    }, {} as { [key: string]: number });

    const topCategoryEntry = Object.entries(categoryTotals)
      .sort((a, b) => (b[1] as number) - (a[1] as number))[0];

    this.topCategory = topCategoryEntry ? topCategoryEntry[0] : '';
  }

  private calculateCategoryDistribution(categoryBreakdown: { [key: string]: number }) {
    if (!categoryBreakdown) return;

    const total = Object.values(categoryBreakdown).reduce((sum, amount) => sum + amount, 0);

    this.categoryDistribution = Object.entries(categoryBreakdown)
      .map(([name, amount]) => ({
        name,
        amount,
        percentage: total > 0 ? Math.round((amount / total) * 100) : 0
      }))
      .sort((a, b) => b.amount - a.amount);
  }

  private generateMockInsights(): string {
    const categories = Object.keys(this.categoryDistribution);
    const topCategory = categories[0] || 'OTHER';
    const total = this.totalAmount;

    return `Based on your ${this.expenseCount} expenses totaling $${total.toFixed(2)}, here are my insights:

🎯 **Spending Patterns:**
- Your largest spending category is ${topCategory.toLowerCase()}, accounting for ${this.categoryDistribution[0]?.percentage || 0}% of your total expenses
- You're averaging $${this.monthlyAverage.toFixed(2)} per month in expenses

💡 **Smart Recommendations:**
- Consider setting a monthly budget for ${topCategory.toLowerCase()} to better control your spending
- Review recurring expenses and identify any subscriptions you might not be using
- Try tracking cash expenses more consistently for complete financial visibility

📈 **Optimization Tips:**
- Meal planning could save you 20-30% on food expenses
- Bundle similar services to reduce subscription costs
- Consider cashback credit cards for everyday purchases

Remember: Small, consistent changes often lead to the biggest financial improvements over time!`;
  }
}
