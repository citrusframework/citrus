# Citrus site

This directory contains the code for the Citrus website, [citrusframework.org](http://citrusframework.org/).

## Contributing

For information about contributing, see the [Contributing page](http://citrusframework.org/docs/contributing/).

## Running locally

You can preview your contributions before opening a pull request by running from within the directory:

```
mvn clean resources:resources package -Psite,release-local -N
```

It's just a jekyll site, after all! :wink:
