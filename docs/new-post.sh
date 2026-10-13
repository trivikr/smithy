#!/usr/bin/env bash
# Scaffolds a new Smithy blog post under source-blog/posts/<year>/<slug>.md.
#
# Usage: ./new-post.sh "Post Title" [Category] [author-key]
#   Category    Announcement | Release | Guide (default: Announcement)
#   author-key  A key from blog_authors in source-blog/conf.py (default: smithy)
#
# Also available as: make new-post TITLE="Post Title" CATEGORY=Release
set -euo pipefail

title="${1:-}"
category="${2:-Announcement}"
author="${3:-smithy}"

if [[ -z "$title" ]]; then
    echo "usage: $0 \"Post Title\" [Category] [author-key]" >&2
    exit 1
fi
[[ -n "$category" ]] || category="Announcement"
[[ -n "$author" ]] || author="smithy"

# Front matter is YAML; keep values to a safe character set so a title or
# category can't break out of it.
if [[ "$category" =~ [^A-Za-z0-9\ -] || "$author" =~ [^a-z0-9_-] ]]; then
    echo "error: category must be alphanumeric; author must be a blog_authors key" >&2
    exit 1
fi

slug="$(printf '%s' "$title" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/-/g; s/^-+//; s/-+$//')"
if [[ -z "$slug" ]]; then
    echo "error: could not derive a file name from title: $title" >&2
    exit 1
fi

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
year="$(date +%Y)"
post_dir="$script_dir/source-blog/posts/$year"
post_file="$post_dir/$slug.md"

if [[ -e "$post_file" ]]; then
    echo "error: $post_file already exists" >&2
    exit 1
fi

mkdir -p "$post_dir"
# The title is written as a Markdown heading, not into the YAML front matter.
cat > "$post_file" <<EOF
---
date: $(date +%Y-%m-%d)
author: $author
category: $category
tags:
---

# $title

:::{admonition} TL;DR
:class: tldr

One or two sentences: what changed and why it matters to Smithy users.
:::

## The problem

## The solution

## How it works

## Get started

## What's next
EOF

echo "Created ${post_file#"$script_dir/"}"
echo "Note: posts dated in the future are treated as drafts and are not listed."
