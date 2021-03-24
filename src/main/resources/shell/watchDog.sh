#!/bin/sh

echo 'mainPid:' ${mainPid}
echo 'extensionPids:' ${extensionPids}
echo 'workingDir:' ${workingDir}

main_pid=${mainPid}
extension_pids=(${extensionPids//,/ })
working_dir=${workingDir}

main_proc_is_run() {
  if [ -z '${main_pid}' ]; then
    echo "process have error"
    return 1
  else
    echo "process is ok，pid: ${main_pid}"
    return 0
  fi
}

# 判断主进程
while [ true ]; do
  main_proc_is_run ${main_pid}
  if [ 1 = $? ]; then
    echo "sleeping..."
    sleep 30
  else
    echo "need kill all..."
    break
  fi
done

echo "start kill......."
# 正常移除
i=0
while (($i < 3)); do
  for element in ${extension_pids[@]}; do
    echo "kill -15 "$element
    kill -15 $element
    if [ 0 = $? ]; then
      echo "remove element"$element
      extension_pids=(${extension_pids[*]/$element/})
    fi
  done
  i=$(($i + 1))
  sleep 15
done

# 强制杀掉
for element in ${extension_pids[@]}; do
  echo "kill -9 "$element
  kill -9 $element
done
echo "kill end...."

echo "start clean working...."
echo "delete dir:"$working_dir
rm -rf $working_dir
echo "end clean working...."
exit 0
