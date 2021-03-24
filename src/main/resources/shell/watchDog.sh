#!/bin/sh

echo  'mainPid:' ${mainPid}
echo  'extension_pids:' ${extensionPids}
extension_pids=(${extensionPids//,/ })

main_proc_is_run () {
	if [ -z '${mainPid}' ];then
        echo "process have error"
        return 1
  else
      echo "process is ok，pid: ${mainPid}"
      return 0
  fi
}

# 判断主进程
while [ true ]
  do
    main_proc_is_run ${mainPid}
    if [ 1 = $? ];then
          echo "sleeping..."
          sleep 30
    else
        echo "need kill all..."
        break
    fi
  done

echo "start kill......."

# 正常移除
i=0;
while(($i<3))
do
  for element in ${extension_pids[@]}
    do
      echo "kill -15 "$element
      kill -15 $element
      if [ 0 = $? ];then
          echo "remove element"$element
          extension_pids=( ${extension_pids[*]/$element} )
    fi
    done
  i=$(($i+1))
  sleep 15
done

# 强制杀掉
for element in ${extension_pids[@]}
    do
      echo "kill -9 "$element
      kill -9 $element
    done

echo "end...."
exit 0




