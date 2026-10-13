# Sphinx configuration for the Smithy blog, published at https://smithy.io/blog/.
#
# The blog is its own Sphinx project (rather than a section of the 2.0 docs) so
# that posts are not tied to a documentation version. It reuses the shared
# docs configuration for the theme, header, and branding.

# Include the shared conf.py file.
shared_config = "../conf.py"
with open(shared_config) as file:
    exec(file.read())

extensions.append("ablog")

html_title = "Smithy Blog"
release = u'blog'
version = release

html_theme_options['path_to_docs'] = "docs/source-blog"

# Blog pages are not versioned docs: point the "Documentation" header link at
# the current docs instead of this project's root (see smithy-navbar-nav.html).
html_context = {
    "smithy_docs_url": "/2.0/index.html",
    # Emitted as <link rel="alternate"> in _templates/layout.html.
    "smithy_feed_url": "/blog/archive/atom.xml",
}

# The blog has its own sidebar, so this replaces (rather than extends) the
# shared html_sidebars. Defining a second wildcard pattern alongside "**"
# would make Sphinx warn, and the build treats warnings as errors.
html_sidebars = {
    "**": [
        "ablog/postcard.html",
        "ablog/recentposts.html",
        "ablog/archives.html",
    ],
}

# -- ABlog ------------------------------------------------------------------

blog_title = "Smithy Blog"
blog_baseurl = "https://smithy.io/blog/"
# Every document under posts/ is a blog post; no per-post "blogpost" flag needed.
blog_post_pattern = "posts/*/*"
# ABlog's generated "all posts" listing lives at /blog/archive/ so it does not
# collide with the hand-written landing page (index.md).
blog_path = "archive"
blog_authors = {
    "smithy": ("The Smithy Team", "https://github.com/smithy-lang"),
}
blog_default_author = "smithy"
post_date_format = "%b %d, %Y"
post_date_format_short = "%b %d, %Y"
post_auto_image = 0
post_auto_excerpt = 1
blog_feed_fulltext = True
blog_feed_length = 20
blog_feed_archives = False
# pydata-sphinx-theme already loads Font Awesome.
fontawesome_included = True
