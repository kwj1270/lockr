#!/bin/bash
# Cucumber Feature Drift Checker
# Api 파일 변경 시 대응하는 .feature 파일 업데이트 알림
# 새 도메인의 Api가 추가되었으나 feature 파일이 없으면 감지

FILE="$CLAUDE_FILE_PATH"
[ -z "$FILE" ] && exit 0

FEATURE_DIR="src/test/resources/features"

# 1) *Api.java 또는 *QueryApi.java 변경 감지
if echo "$FILE" | grep -qE '/api/.*Api\.java$'; then

  # domain/{context}/{subdomain}/api/ 에서 subdomain 추출
  SUBDOMAIN=$(echo "$FILE" | sed -n 's|.*/domain/[^/]*/\([^/]*\)/api/.*|\1|p')
  [ -z "$SUBDOMAIN" ] && exit 0

  # subdomain을 kebab-case로 변환 (camelCase → kebab-case)
  FEATURE_NAME=$(echo "$SUBDOMAIN" | sed 's/\([a-z]\)\([A-Z]\)/\1-\2/g' | tr '[:upper:]' '[:lower:]')
  FEATURE_FILE="${FEATURE_DIR}/${FEATURE_NAME}.feature"

  if [ ! -f "$FEATURE_FILE" ]; then
    echo "🥒 [Feature 누락] '${SUBDOMAIN}' 도메인에 Api가 있지만 ${FEATURE_FILE} 파일이 없습니다. Cucumber feature 파일을 작성하세요."
  else
    echo "🥒 [Feature 동기화] '${SUBDOMAIN}' 도메인의 Api가 변경되었습니다. ${FEATURE_FILE}도 함께 업데이트가 필요한지 확인하세요."
  fi
fi

# 2) 새 도메인 디렉토리의 domain/ 하위 AggregateRoot 추가 감지
if echo "$FILE" | grep -qE '/domain/[^/]+/[^/]+/domain/.*\.java$'; then
  if grep -q 'extends AggregateRoot' "$FILE" 2>/dev/null; then
    SUBDOMAIN=$(echo "$FILE" | sed -n 's|.*/domain/[^/]*/\([^/]*\)/domain/.*|\1|p')
    [ -z "$SUBDOMAIN" ] && exit 0

    FEATURE_NAME=$(echo "$SUBDOMAIN" | sed 's/\([a-z]\)\([A-Z]\)/\1-\2/g' | tr '[:upper:]' '[:lower:]')
    FEATURE_FILE="${FEATURE_DIR}/${FEATURE_NAME}.feature"

    if [ ! -f "$FEATURE_FILE" ]; then
      echo "🥒 [Feature 누락] '${SUBDOMAIN}' 도메인에 AggregateRoot가 있지만 ${FEATURE_FILE} 파일이 없습니다. Cucumber feature 파일을 작성하세요."
    fi
  fi
fi
