---
title: History
layout: docs
permalink: "/docs/history/"
---

{% for release in site.data.releases %}
<h2 id="{{ release.tag }}">Release {{ release.version }} / {% if release.date != nil %}{{ release.date }}{% endif %}{% if release.date == nil %}{{ site.time | date: '%Y-%m-%d' }}{% endif %}</h2>

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
{% for change in release.changes %}| {{ change.type }} | [#{{ change.id }}]({% if change.type == "Fix" %}{{ release.issues }}{% endif %}{% if change.type != "Fix" %}{{ release.stories }}{% endif %}{{ change.id }}) | {{ change.title }} | {{ change.author }} |
{% endfor %}

{% endfor %}

## Release 0.0.0 / 2006-05-01
{: #v0-0-0}

- Internal usage @ConSol
- Birthday!
