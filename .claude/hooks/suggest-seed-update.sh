#!/bin/bash
# Seed Data Update Suggestion Hook
# 새 테이블 마이그레이션이 추가되면 seed 데이터 업데이트를 제안

FILE="$CLAUDE_FILE_PATH"
[ -z "$FILE" ] && exit 0

# Flyway 마이그레이션 파일에서 CREATE TABLE 감지
if echo "$FILE" | grep -qE 'db/migration/V[0-9]+.*\.sql$'; then
  if grep -qi 'CREATE TABLE' "$FILE" 2>/dev/null; then
    TABLE_NAME=$(grep -i 'CREATE TABLE' "$FILE" 2>/dev/null | head -1 | sed 's/.*CREATE TABLE[[:space:]]*\`\{0,1\}\([a-z_]*\)\`\{0,1\}.*/\1/')
    echo "💡 새 테이블 '${TABLE_NAME}'이 추가되었습니다. infra/mysql/seed/seed_data.sql에 로컬 개발용 seed 데이터 추가를 검토하세요."
  fi
fi
