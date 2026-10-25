import { expect, test } from '@playwright/test';

import { addBook } from './helpers/books';
import { loginAs } from './helpers/auth';
import { generateRecommendations, recommendationCards } from './helpers/recommendations';

function uniqueEmail(): string {
  return `recs-${crypto.randomUUID().slice(0, 8)}@e2e.test`;
}

const EMPTY_STATE_MESSAGE =
  'No recommendations to review yet. Use the generate button above to discover new books!';

test.beforeEach(async ({ page }) => {
  await loginAs(page, uniqueEmail());
  await page.goto('/app/recommendations');
});

test('generate button is present on the recommendations page', async ({ page }) => {
  await expect(page.getByRole('button', { name: 'Generate recommendations' })).toBeVisible();
});

test('with 0 read books, generate button is disabled and shows a warning', async ({ page }) => {
  await expect(page.getByRole('button', { name: 'Generate recommendations' })).toBeDisabled();
  await expect(page.getByText('Add 3 more books to generate recommendations')).toBeVisible();
});

test('with 1 read book, generate button is still disabled', async ({ page }) => {
  await addBook(page, 'Dune');
  await page.goto('/app/recommendations');

  await expect(page.getByRole('button', { name: 'Generate recommendations' })).toBeDisabled();
  await expect(page.getByText('Add 2 more books to generate recommendations')).toBeVisible();
});

test('with 2 read books, generate button is still disabled', async ({ page }) => {
  await addBook(page, 'Dune');
  await addBook(page, 'Neuromancer');
  await page.goto('/app/recommendations');

  await expect(page.getByRole('button', { name: 'Generate recommendations' })).toBeDisabled();
  await expect(page.getByText('Add 1 more book to generate recommendations')).toBeVisible();
});

test('with 3 read books, generate button becomes enabled', async ({ page }) => {
  await addBook(page, 'Dune');
  await addBook(page, 'Neuromancer');
  await addBook(page, 'The Left Hand of Darkness');
  await page.goto('/app/recommendations');

  await expect(page.getByRole('button', { name: 'Generate recommendations' })).toBeEnabled();
});

test('clicking generate shows a loading indicator', async ({ page }) => {
  await addBook(page, 'Dune');
  await addBook(page, 'Neuromancer');
  await addBook(page, 'The Left Hand of Darkness');
  await page.goto('/app/recommendations');
  await page.getByRole('button', { name: 'Generate recommendations' }).click();

  await expect(page.getByRole('progressbar', { name: 'Generating recommendations' })).toBeVisible();
});

test('after loading, 5 recommendation cards are displayed', async ({ page }) => {
  await generateRecommendations(page);

  await expect(recommendationCards(page)).toHaveCount(5);
});

test('each card shows book title, author, and description text', async ({ page }) => {
  await generateRecommendations(page);

  const cards = await recommendationCards(page).all();

  for (const card of cards) {
    await expect(card.getByTestId('book-title')).not.toBeEmpty();
    await expect(card.getByTestId('book-author')).not.toBeEmpty();
    await expect(card.getByTestId('description')).not.toBeEmpty();
  }
});

test('clicking Save on a recommendation card removes it from the list', async ({ page }) => {
  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  const bookTitle = await card.getByTestId('book-title').innerText();
  await card.getByRole('button', { name: 'Save for later' }).click();

  await expect(recommendationCards(page)).toHaveCount(4);
  await expect(recommendationCards(page).filter({ hasText: bookTitle })).toHaveCount(0);
});

test('clicking "Don\'t suggest again" removes the card from the list', async ({ page }) => {
  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  const bookTitle = await card.getByTestId('book-title').innerText();
  await card.getByRole('button', { name: "Don't suggest again" }).click();

  await expect(recommendationCards(page)).toHaveCount(4);
  await expect(recommendationCards(page).filter({ hasText: bookTitle })).toHaveCount(0);
});

test('a blocked book does not reappear in recommendations after reloading the page', async ({
  page,
}) => {
  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  const bookTitle = await card.getByTestId('book-title').innerText();
  await card.getByRole('button', { name: "Don't suggest again" }).click();
  await expect(recommendationCards(page)).toHaveCount(4);

  await page.reload();

  await expect(recommendationCards(page)).toHaveCount(4);
  await expect(recommendationCards(page).filter({ hasText: bookTitle })).toHaveCount(0);
});

test('a saved book does not reappear in recommendations after reloading the page', async ({
  page,
}) => {
  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  const bookTitle = await card.getByTestId('book-title').innerText();
  await card.getByRole('button', { name: 'Save for later' }).click();
  await expect(recommendationCards(page)).toHaveCount(4);

  await page.reload();

  await expect(recommendationCards(page)).toHaveCount(4);
  await expect(recommendationCards(page).filter({ hasText: bookTitle })).toHaveCount(0);
});

test('reviewing all recommendations shows the empty state', async ({ page }) => {
  await generateRecommendations(page);

  for (let i = 0; i < 2; i++) {
    await recommendationCards(page).first().getByRole('button', { name: 'Save for later' }).click();
  }

  for (let i = 0; i < 3; i++) {
    await recommendationCards(page)
      .first()
      .getByRole('button', { name: "Don't suggest again" })
      .click();
  }

  await expect(recommendationCards(page)).toHaveCount(0);
  await expect(page.getByText(EMPTY_STATE_MESSAGE)).toBeVisible();
});

test('a failed save restores the card and shows an error message', async ({ page }) => {
  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  const bookTitle = await card.getByTestId('book-title').innerText();

  await page.route('**/api/v1/saved-books', (route) => route.abort());
  await card.getByRole('button', { name: 'Save for later' }).click();

  await expect(page.getByText('Failed to save book.')).toBeVisible();
  await expect(recommendationCards(page)).toHaveCount(5);
  await expect(recommendationCards(page).filter({ hasText: bookTitle })).toHaveCount(1);
});

test('a failed block restores the card and shows an error message', async ({ page }) => {
  await generateRecommendations(page);

  const card = recommendationCards(page).first();
  const bookTitle = await card.getByTestId('book-title').innerText();

  await page.route('**/api/v1/blocked-books', (route) => route.abort());
  await card.getByRole('button', { name: "Don't suggest again" }).click();

  await expect(page.getByText('Failed to block book.')).toBeVisible();
  await expect(recommendationCards(page)).toHaveCount(5);
  await expect(recommendationCards(page).filter({ hasText: bookTitle })).toHaveCount(1);
});
