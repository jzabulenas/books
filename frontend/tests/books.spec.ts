import { expect, test } from '@playwright/test';

import { addBook } from './helpers/books';
import { loginAs } from './helpers/auth';

function uniqueEmail(): string {
  return `books-${crypto.randomUUID().slice(0, 8)}@e2e.test`;
}

test.beforeEach(async ({ page }) => {
  await loginAs(page, uniqueEmail());
});

test('books page loads with an empty list initially', async ({ page }) => {
  await expect(page.getByRole('heading', { name: 'My Books' })).toBeVisible();
  await expect(page.getByText('0 books added')).toBeVisible();
  await expect(page.getByRole('list', { name: 'Read books' })).not.toBeVisible();
});

test('adding a book shows it as a chip in the list', async ({ page }) => {
  await addBook(page, 'Dune');

  await expect(page.getByRole('listitem').filter({ hasText: 'Dune' })).toBeVisible();
  await expect(page.getByText('1 book added')).toBeVisible();
});

test('adding a second book appends to the list', async ({ page }) => {
  await addBook(page, 'Dune');
  await addBook(page, 'Neuromancer');

  await expect(page.getByRole('listitem').filter({ hasText: 'Dune' })).toBeVisible();
  await expect(page.getByRole('listitem').filter({ hasText: 'Neuromancer' })).toBeVisible();
  await expect(page.getByText('2 books added')).toBeVisible();
});

test('submitting a blank title shows a validation error', async ({ page }) => {
  await page.getByRole('button', { name: 'Add' }).click();

  await expect(page.getByText('Book title is required')).toBeVisible();
});

test('adding a book that already exists shows an error', async ({ page }) => {
  await addBook(page, 'Dune');
  await addBook(page, 'Dune');

  await expect(page.getByRole('alert')).toBeVisible();
});

test('clicking the remove button on a chip removes it from the list', async ({ page }) => {
  await addBook(page, 'Dune');
  await page.getByRole('button', { name: 'Remove Dune' }).click();

  await expect(page.getByRole('listitem').filter({ hasText: 'Dune' })).not.toBeVisible();
  await expect(page.getByText('0 books added')).toBeVisible();
});
