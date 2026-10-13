# Smithy specification

This package is the Smithy website, specification, and project documentation.


## Building

The Smithy docs are built using [Sphinx](https://www.sphinx-doc.org/en/master),
which requires python3.

Once you have python3 installed, you need to install the dependencies and local
modules:

```
make install
```

Then to build static output, run:

```
make html
```

To view the output, run:

```
make serve
```

## Blog

The blog at https://smithy.io/blog/ is a separate Sphinx project in
`source-blog/`, built with [ABlog](https://ablog.readthedocs.io/) and written
in Markdown ([MyST](https://myst-parser.readthedocs.io/)). Posts live in
`source-blog/posts/<year>/<slug>.md` and are listed on the blog index
automatically.

To add a post:

1. Scaffold it (category is `Announcement`, `Release`, or `Guide`):

   ```
   make new-post TITLE="My Post Title" CATEGORY=Release
   ```

2. Write the content, then build with `make htmlblog` (or `make dev-blog` for
   live reload).

Posts dated in the future are treated as drafts and aren't listed, so set the
`date` in the front matter to the publish date. Blog posts are public: only
link to public resources such as smithy.io, GitHub, and public documentation.
