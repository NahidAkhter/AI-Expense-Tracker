import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import {
  BehaviorSubject,
  Observable,
  catchError,
  map,
  tap,
  throwError,
  switchMap,
  shareReplay
} from 'rxjs';
import { Expense } from '../interfaces/expense';
import { ExpenseSummary } from '../interfaces/expense-summary';

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ExpenseService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/expenses';

  private expensesSubject = new BehaviorSubject<Expense[]>([]);
  public expenses$ = this.expensesSubject.asObservable().pipe(shareReplay(1));

  public categories$ = this.expenses$.pipe(
    map(expenses => this.calculateCategoryBreakdown(expenses))
  );

  public summary$ = this.expenses$.pipe(
    map(expenses => this.calculateSummary(expenses))
  );

  constructor() {
    this.loadExpenses().subscribe();
  }

  private loadExpenses(): Observable<Expense[]> {
    return this.http.get<ApiResponse<Expense[]>>(this.apiUrl).pipe(
      map(response => response.data ?? []),
      tap(expenses => this.expensesSubject.next(expenses)),
      catchError(this.handleError)
    );
  }

  addExpense(expense: Omit<Expense, 'id'>): Observable<Expense> {
    return this.http.post<ApiResponse<Expense>>(this.apiUrl, expense).pipe(
      map(response => response.data as Expense),
      tap(newExpense => {
        const current = this.expensesSubject.value;
        this.expensesSubject.next([...current, newExpense]);
      }),
      catchError(this.handleError)
    );
  }

  deleteExpense(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`).pipe(
      map(() => undefined),
      tap(() => {
        const current = this.expensesSubject.value;
        this.expensesSubject.next(current.filter(expense => expense.id !== id));
      }),
      catchError(this.handleError)
    );
  }

  getInsights(): Observable<string> {
    return this.http.get<ApiResponse<string>>(`${this.apiUrl}/insights`).pipe(
      map(response => response.data ?? ''),
      catchError(this.handleError)
    );
  }

  private calculateCategoryBreakdown(expenses: Expense[]): { [category: string]: number } {
    return expenses.reduce((acc, expense) => {
      acc[expense.category] = (acc[expense.category] || 0) + expense.amount;
      return acc;
    }, {} as { [category: string]: number });
  }

  private calculateSummary(expenses: Expense[]): ExpenseSummary {
    const total = expenses.reduce((sum, expense) => sum + expense.amount, 0);
    const categoryBreakdown = this.calculateCategoryBreakdown(expenses);

    // Simple monthly trend calculation
    const monthlyTrend = expenses.reduce((acc, expense) => {
      const month = new Date(expense.date).toISOString().substring(0, 7); // YYYY-MM
      acc[month] = (acc[month] || 0) + expense.amount;
      return acc;
    }, {} as { [month: string]: number });

    const monthlyTrendArray = Object.entries(monthlyTrend)
      .map(([month, amount]) => ({ month, amount }))
      .sort((a, b) => a.month.localeCompare(b.month));

    return {
      total,
      categoryBreakdown,
      monthlyTrend: monthlyTrendArray
    };
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unexpected error occurred';

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Client Error: ${error.error.message}`;
    } else {
      switch (error.status) {
        case 0:
          errorMessage = 'Unable to connect to server. Please check your connection.';
          break;
        case 404:
          errorMessage = 'Requested resource not found.';
          break;
        case 500:
          errorMessage = 'Server error. Please try again later.';
          break;
        default:
          errorMessage = `Error ${error.status}: ${error.message}`;
      }
    }

    console.error('ExpenseService error:', error);
    return throwError(() => new Error(errorMessage));
  }
}
