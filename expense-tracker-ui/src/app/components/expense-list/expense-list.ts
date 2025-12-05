import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ExpenseService } from '../../services/expense-service';
import { NotificationService } from '../../services/notification-service';

@Component({
  selector: 'app-expense-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './expense-list.html',
  styleUrls: ['./expense-list.scss'],
})
export class ExpenseListComponent {
  private expenseService = inject(ExpenseService);
  private notificationService = inject(NotificationService);

  expenses: any[] = [];
  filteredExpenses: any[] = [];
  categories: string[] = [];

  newExpense = {
    description: '',
    amount: 0
  };

  selectedCategory = '';
  sortBy = 'date-desc';
  totalAmount = 0;
  addingExpense = false;
  deletingId: number | null = null;

  ngOnInit() {
    this.expenseService.expenses$.subscribe(expenses => {
      this.expenses = expenses;
      this.applyFilters();
      this.updateCategories();
      this.calculateTotal();
    });
  }

  onSubmit() {
    if (this.newExpense.description && this.newExpense.amount > 0) {
      this.addingExpense = true;

      this.expenseService.addExpense({
        description: this.newExpense.description,
        amount: this.newExpense.amount,
        date: new Date().toISOString(),
        category: 'OTHER' // Will be categorized by AI
      }).subscribe({
        next: () => {
          this.clearForm();
          this.notificationService.show({
            type: 'success',
            message: 'Expense added successfully!'
          });
        },
        error: (error) => {
          this.notificationService.show({
            type: 'error',
            message: 'Failed to add expense: ' + error.message
          });
        },
        complete: () => {
          this.addingExpense = false;
        }
      });
    }
  }

  deleteExpense(id: number) {
    if (confirm('Are you sure you want to delete this expense?')) {
      this.deletingId = id;
      this.expenseService.deleteExpense(id).subscribe({
        next: () => {
          this.notificationService.show({
            type: 'success',
            message: 'Expense deleted successfully!'
          });
        },
        error: (error) => {
          this.notificationService.show({
            type: 'error',
            message: 'Failed to delete expense: ' + error.message
          });
        },
        complete: () => {
          this.deletingId = null;
        }
      });
    }
  }

  clearForm() {
    this.newExpense = {
      description: '',
      amount: 0
    };
  }

  applyFilters() {
    let filtered = [...this.expenses];

    // Category filter
    if (this.selectedCategory) {
      filtered = filtered.filter(expense =>
        expense.category === this.selectedCategory
      );
    }

    // Sort
    filtered.sort((a, b) => {
      switch (this.sortBy) {
        case 'date-desc':
          return new Date(b.date).getTime() - new Date(a.date).getTime();
        case 'date-asc':
          return new Date(a.date).getTime() - new Date(b.date).getTime();
        case 'amount-desc':
          return b.amount - a.amount;
        case 'amount-asc':
          return a.amount - b.amount;
        default:
          return 0;
      }
    });

    this.filteredExpenses = filtered;
  }

  clearFilters() {
    this.selectedCategory = '';
    this.sortBy = 'date-desc';
    this.applyFilters();
  }

  private updateCategories() {
    const uniqueCategories = [...new Set(this.expenses.map(e => e.category))];
    this.categories = uniqueCategories.sort();
  }

  private calculateTotal() {
    this.totalAmount = this.expenses.reduce((sum, expense) => sum + expense.amount, 0);
  }
}
