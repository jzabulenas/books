import { type Page, expect, test } from '@playwright/test';

import { addBook } from './books';

export function recommendationCards(page: Page) {
  return page.getByRole('list').getByRole('listitem');
}

export async function generateRecommendations(page: Page): Promise<void> {
  test.setTimeout(60_000);

  await addBook(page, 'Dune');
  await addBook(page, 'Neuromancer');
  await addBook(page, 'The Left Hand of Darkness');

  const initialLoad = page.waitForResponse(
    (response) =>
      response.url().endsWith('/api/v1/recommendations') && response.request().method() === 'GET',
  );
  await page.goto('/app/recommendations');
  await initialLoad;

  await page.getByRole('button', { name: 'Generate recommendations' }).click();
  await expect(recommendationCards(page)).toHaveCount(5, { timeout: 45_000 });
}
