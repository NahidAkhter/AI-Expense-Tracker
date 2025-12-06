import { TestBed } from '@angular/core/testing';

import {apiInterceptor } from './api-interceptor';

describe('apiInterceptor', () => {
  let service: typeof apiInterceptor;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(apiInterceptor);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
