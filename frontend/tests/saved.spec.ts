import { expect, test } from '@playwright/test';

import { loginAs } from './helpers/auth';
import { generateRecommendations, recommendationCards } from './helpers/recommendations';

function uniqueEmail(): string {
  return `saved-${crypto.randomUUID().slice(0, 8)}@e2e.test`;
}

const EMPTY_STATE_MESSAGE = 'No saved books yet. Head to Recommendations to discover new ones!';

test.beforeEach(async ({ page }) => {
  await loginAs(page, uniqueEmail());
});

test('saved page shows an empty state message when no books saved', async ({ page }) => {
  await page.goto('/app/saved');

  await expect(page.getByText(EMPTY_STATE_MESSAGE)).toBeVisible();
});

test('after saving a book via the recommendations flow, it appears on the saved page', async ({
  page,
}) => {
  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  const bookTitle = await card.getByTestId('book-title').innerText();
  await card.getByRole('button', { name: 'Save for later' }).click();

  await page.goto('/app/saved');

  await expect(page.getByTestId('book-title').filter({ hasText: bookTitle })).toBeVisible();
});

test('removing a saved book removes it from the list and shows the empty state again', async ({
  page,
}) => {
  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  const bookTitle = await card.getByTestId('book-title').innerText();
  await card.getByRole('button', { name: 'Save for later' }).click();

  await page.goto('/app/saved');
  await expect(page.getByTestId('book-title').filter({ hasText: bookTitle })).toBeVisible();

  await page.getByRole('button', { name: 'Remove' }).click();

  await expect(page.getByTestId('book-title').filter({ hasText: bookTitle })).not.toBeVisible();
  await expect(page.getByText(EMPTY_STATE_MESSAGE)).toBeVisible();
});

test('a removed saved book is not suggested in the next generation', async ({ page }) => {
  test.setTimeout(90_000);

  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  const bookTitle = await card.getByTestId('book-title').innerText();
  await card.getByRole('button', { name: 'Save for later' }).click();

  await page.goto('/app/saved');
  await page.getByRole('button', { name: 'Remove' }).click();
  await expect(page.getByText(EMPTY_STATE_MESSAGE)).toBeVisible();

  await page.goto('/app/recommendations');
  const generation = page.waitForResponse(
    (response) =>
      response.url().endsWith('/api/v1/recommendations/generate') &&
      response.request().method() === 'POST',
  );
  await page.getByRole('button', { name: 'Generate recommendations' }).click();
  await generation;

  await expect(recommendationCards(page)).toHaveCount(5);
  await expect(recommendationCards(page).filter({ hasText: bookTitle })).toHaveCount(0);
});

test('marking a saved book as read moves it to My Books and out of the saved list', async ({
  page,
}) => {
  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  const bookTitle = await card.getByTestId('book-title').innerText();
  await card.getByRole('button', { name: 'Save for later' }).click();

  await page.goto('/app/saved');
  await expect(page.getByTestId('book-title').filter({ hasText: bookTitle })).toBeVisible();

  await page.getByRole('button', { name: 'Mark as read' }).click();

  await expect(page.getByTestId('book-title').filter({ hasText: bookTitle })).not.toBeVisible();
  await expect(page.getByText(EMPTY_STATE_MESSAGE)).toBeVisible();

  await page.goto('/app/books');
  await expect(page.getByRole('listitem').filter({ hasText: bookTitle })).toBeVisible();
  await expect(page.getByText('4 books added')).toBeVisible();
});
