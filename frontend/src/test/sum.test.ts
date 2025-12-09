import { describe, it, expect } from 'vitest';
import { sum } from '../components/utils/sum';

describe('sum', () => {
  it('adds numbers', () => {
    expect(sum(1, 2)).toBe(3);
  });
});
