#!/bin/bash

REPO_URL="${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}"
MAX_CHARS="${1:-4000}"
REPO_BRANCH="${2:-master}"

get_latest_tag() {
    git describe --tags --abbrev=0 $REPO_BRANCH 2>/dev/null
}

get_commits_since() {
    local tag="$1"
    local rev_range
    if [ -n "$tag" ]; then
        rev_range="$tag..$REPO_BRANCH"
    else
        rev_range="$REPO_BRANCH"
    fi

    git log --format=%s "$rev_range" | grep -v -e '^.$' \
        -e "^Merge branch '$REPO_BRANCH'" -e "^Merge pull request #"
}

format_markdown_list() {
    local commits=("$@")
    local result=""
    local total_len=0
    local max_len=$MAX_CHARS

    local compare_link=""
    if [ -n "$latest_tag" ]; then
        compare_link="[See all changes here](${REPO_URL}/compare/${latest_tag}...$REPO_BRANCH)"
    fi

    local reserved_len=$(( ${#compare_link} + 1 ))

    local line formatted len
    for line in "${commits[@]}"; do
        formatted="- $line"$'\n'
        len=${#formatted}

        if (( total_len + len + reserved_len > max_len )); then
            break
        fi

        result+="$formatted"
        total_len=$(( total_len + len ))
    done

    if [ -n "$compare_link" ]; then
        result+=$'\n'"$compare_link"
    fi

    printf "%s" "$result"
}

main() {
    latest_tag=$(get_latest_tag)
    mapfile -t commits < <(get_commits_since "$latest_tag")

    if [ ${#commits[@]} -eq 0 ]; then
        echo "No commits found since the latest tag."
        exit 0
    fi

    changelog_md=$(format_markdown_list "${commits[@]}")
    echo "$changelog_md"
}

main