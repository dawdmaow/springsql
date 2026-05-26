CREATE TABLE IF NOT EXISTS authors (
	id INTEGER PRIMARY KEY,
	name TEXT NOT NULL,
	country TEXT NOT NULL,
	born_year INTEGER
);

CREATE TABLE IF NOT EXISTS books (
	id INTEGER PRIMARY KEY,
	author_id INTEGER NOT NULL,
	title TEXT NOT NULL,
	genre TEXT NOT NULL,
	published_year INTEGER NOT NULL,
	in_print INTEGER NOT NULL DEFAULT 1 CHECK (in_print IN (0, 1)),
	price_cents INTEGER NOT NULL,
	FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reviews (
	id INTEGER PRIMARY KEY,
	book_id INTEGER NOT NULL,
	reviewer_name TEXT NOT NULL,
	rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
	review_text TEXT NOT NULL,
	created_at TEXT NOT NULL,
	FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_books_title ON books(title);
CREATE INDEX IF NOT EXISTS idx_reviews_book_id ON reviews(book_id);

CREATE VIEW IF NOT EXISTS book_catalog AS
SELECT
	b.id,
	b.title,
	a.name AS author,
	b.genre,
	b.published_year,
	b.price_cents,
	b.in_print
FROM books b
JOIN authors a ON a.id = b.author_id;

INSERT OR IGNORE INTO authors (id, name, country, born_year) VALUES
	(1, 'J.R.R. Tolkien', 'United Kingdom', 1892),
	(2, 'Ursula K. Le Guin', 'United States', 1929),
	(3, 'Octavia E. Butler', 'United States', 1947);

INSERT OR IGNORE INTO books (id, author_id, title, genre, published_year, in_print, price_cents) VALUES
	(1, 1, 'The Hobbit', 'Fantasy', 1937, 1, 1599),
	(2, 1, 'The Lord of the Rings', 'Fantasy', 1954, 1, 2599),
	(3, 2, 'A Wizard of Earthsea', 'Fantasy', 1968, 1, 1499),
	(4, 2, 'The Left Hand of Darkness', 'Science Fiction', 1969, 1, 1699),
	(5, 3, 'Kindred', 'Science Fiction', 1979, 1, 1399);

INSERT OR IGNORE INTO reviews (id, book_id, reviewer_name, rating, review_text, created_at) VALUES
	(1, 1, 'Mina', 5, 'Comfort read, still perfect.', '2026-05-01 09:15:00'),
	(2, 3, 'Jon', 4, 'Quick, sharp, and easy to recommend.', '2026-05-03 14:40:00'),
	(3, 4, 'Rae', 5, 'Still feels bold decades later.', '2026-05-08 18:05:00'),
	(4, 5, 'Tess', 5, 'Time travel with real emotional weight.', '2026-05-12 11:20:00');
