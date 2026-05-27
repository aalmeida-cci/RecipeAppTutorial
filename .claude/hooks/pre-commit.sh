#!/bin/bash
# Claude Code PreToolUse hook: lint-gate before `git commit`.
# Reads the tool-call JSON from stdin, inspects the Bash command,
# and blocks the commit (exit 2) if lint fails.

set -u

INPUT="$(cat)"
COMMAND="$(printf '%s' "$INPUT" | jq -r '.tool_input.command // ""')"

# Only gate actual commit invocations. Allow everything else through.
if ! printf '%s' "$COMMAND" | grep -Eq '(^|[^A-Za-z0-9_])git[[:space:]]+commit([[:space:]]|$)'; then
  exit 0
fi

# Skip when the caller explicitly bypasses hooks.
if printf '%s' "$COMMAND" | grep -Eq '(--no-verify|-n([[:space:]]|$))'; then
  exit 0
fi

cd "$(git rev-parse --show-toplevel 2>/dev/null)" || exit 0

echo "🔍 Pre-commit: running lint gate..." >&2
./gradlew :composeApp:lintDebug :composeApp:ktlintCheck --daemon --quiet
STATUS=$?

if [ $STATUS -ne 0 ]; then
  {
    echo ""
    echo "❌ LINT FAILURE: lintDebug or ktlintCheck reported errors."
    echo "Fix the issues above before committing, or re-run the commit with --no-verify to bypass."
  } >&2
  exit 2
fi

echo "✅ Lint passed!" >&2
exit 0
