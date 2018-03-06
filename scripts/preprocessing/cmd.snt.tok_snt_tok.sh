#!/bin/bash
set -ueo pipefail

for AMR in "${TRAIN_FILE}" "${DEV_FILE}" "${TEST_FILE}"
do
    cat "${AMR}" | grep '::snt ' | sed 's/^# ::snt //' | sed 's/   */ /g' > "${AMR}.snt.tok"
done
