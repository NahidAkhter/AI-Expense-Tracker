import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Insights } from './insights';

describe('Insights', () => {
  let component: Insights;
  let fixture: ComponentFixture<Insights>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Insights]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Insights);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
