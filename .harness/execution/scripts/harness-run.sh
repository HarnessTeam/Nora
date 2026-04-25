#!/usr/bin/env bash
# .harness Execution Layer — 统一执行入口
# 用法: bash .harness/execution/scripts/harness-run.sh <pipeline> [args...]
#
# 示例:
#   bash harness-run.sh dev-step "Phase 1 Step 7 — Instrument 测试"
#   bash harness-run.sh build bundledDebug
#   bash harness-run.sh test unit
#   bash harness-run.sh review
#   bash harness-run.sh github issue-create "title" "body"

set -euo pipefail

HARNESS_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_ROOT/.." && pwd)"

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() { echo -e "${BLUE}[harness]${NC} $*"; }
ok()  { echo -e "${GREEN}[✓]${NC} $*"; }
err() { echo -e "${RED}[✗]${NC} $*"; }
warn(){ echo -e "${YELLOW}[!]${NC} $*"; }

# 加载上下文
load_context() {
    log "加载记忆层..."
    if [[ -f "$HARNESS_ROOT/memory/context.yaml" ]]; then
        ok "context.yaml 已加载"
    else
        err "context.yaml 不存在"
        exit 1
    fi
}

# 执行构建
run_build() {
    local variant="${1:-bundledDebug}"
    log "构建变体: $variant"
    cd "$PROJECT_ROOT"
    if [[ -f "build.bat" ]]; then
        cmd /c "set BUILD_VARIANT=$variant && build.bat"
    else
        gradlew.bat "assemble$variant"
    fi
}

# 执行测试
run_test() {
    local type="${1:-unit}"
    cd "$PROJECT_ROOT"
    case "$type" in
        unit)
            log "运行单元测试..."
            gradlew.bat test
            ;;
        instrument)
            log "运行 Instrument 测试..."
            gradlew.bat connectedAndroidTest
            ;;
        *)
            err "未知测试类型: $type"
            exit 1
            ;;
    esac
}

# Git 操作
run_git() {
    local action="${1:-status}"
    cd "$PROJECT_ROOT"
    case "$action" in
        status)  git status --short ;;
        diff)    git diff ;;
        log)     git log --oneline -20 ;;
        push)    git push origin master ;;
        *)
            err "未知 git 操作: $action"
            exit 1
            ;;
    esac
}

# GitHub 操作
run_github() {
    local action="${1:-help}"
    shift || true
    case "$action" in
        issue-list)
            gh issue list --repo HarnessTeam/Nora
            ;;
        issue-create)
            local title="${1:-}"
            local body="${2:-}"
            if [[ -z "$title" ]]; then
                err "需要 issue 标题"
                exit 1
            fi
            gh issue create --repo HarnessTeam/Nora --title "$title" --body "$body"
            ;;
        pr-create)
            local title="${1:-}"
            local body="${2:-}"
            gh pr create --repo HarnessTeam/Nora --title "$title" --body "$body" --base master
            ;;
        *)
            echo "GitHub 操作: issue-list, issue-create, pr-create"
            ;;
    esac
}

# 记录反馈
record_feedback() {
    local pipeline="$1"
    local status="$2"
    local duration="$3"
    local metrics_file="$HARNESS_ROOT/feedback/metrics.yaml"

    local timestamp
    timestamp=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

    cat >> "$metrics_file" << EOF

- timestamp: "$timestamp"
  pipeline: "$pipeline"
  status: "$status"
  duration: ${duration}s
EOF
    ok "反馈已记录到 metrics.yaml"
}

# 主入口
main() {
    local pipeline="${1:-help}"
    shift || true

    local start_time
    start_time=$(date +%s)

    load_context

    case "$pipeline" in
        dev-step)
            log "执行开发步骤管线: $*"
            # 实际由 Claude Code Agent 编排执行
            log "此管线由 Claude Code Agent 驱动，请在 Claude Code 中使用"
            ;;
        build)
            run_build "$@"
            ;;
        test)
            run_test "$@"
            ;;
        git)
            run_git "$@"
            ;;
        github)
            run_github "$@"
            ;;
        review)
            log "代码审查管线 — 由 Claude Code Agent 驱动"
            ;;
        evolve)
            log "自演化管线 — 由 Claude Code Agent 驱动"
            ;;
        help|*)
            echo ""
            echo "╔══════════════════════════════════════════════╗"
            echo "║  .harness — Nora 自动化框架执行入口          ║"
            echo "╚══════════════════════════════════════════════╝"
            echo ""
            echo "用法: bash harness-run.sh <command> [args...]"
            echo ""
            echo "命令:"
            echo "  build [variant]     编译 (default: bundledDebug)"
            echo "  test [unit|instrument] 运行测试"
            echo "  git [status|diff|log|push] Git 操作"
            echo "  github [issue-list|issue-create|pr-create] GitHub 操作"
            echo "  dev-step            开发步骤 (Agent 驱动)"
            echo "  review              代码审查 (Agent 驱动)"
            echo "  evolve              自演化 (Agent 驱动)"
            echo "  help                显示此帮助"
            echo ""
            ;;
    esac

    local end_time
    end_time=$(date +%s)
    local duration=$((end_time - start_time))

    record_feedback "$pipeline" "success" "$duration"
}

main "$@"
