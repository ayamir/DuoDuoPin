#! python3
# -*- coding:utf-8 -*-

import paramiko

if __name__ == '__main__':
    username = 'z217'
    host = '123.57.12.189'
    port = '22'
    password = 'zzhswdmz'

    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(host, port, username, password, timeout=30)
    ssh.exec_command(r'cd duoduopin;./run.sh')
    ssh.close()
