# SAFEGUARD — Security Policy

## Reporting a vulnerability

Please do not disclose security vulnerabilities, sensitive notification content, credentials, or signing keys in public GitHub issues or pull requests.

Report security issues privately to the repository owner using GitHub's **Report a vulnerability** feature, if enabled under **Settings → Security → Private vulnerability reporting**. If this feature is unavailable, contact the owner through an established private channel before sharing technical details.

## Supported versions

SAFEGUARD is a prototype. No release is currently guaranteed to receive security updates. Fixes are evaluated on the active development branch.

## Handling secrets

Never commit Android signing keys, passwords, API tokens, local.properties, .env files, or user notification data. Use GitHub Actions secrets for any future credentials required by CI. If a secret is exposed, revoke or rotate it; removing it from the latest commit alone is insufficient.

## Disclosure and permissions

SAFEGUARD provides local configuration guidance and notification inspection; it is not an antivirus product. Android package visibility and notification access should be requested only when required, explained clearly, and reviewed before distribution.
