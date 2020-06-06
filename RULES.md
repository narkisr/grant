# Rule 1

Dont allow access to entire set of folders:

  foo ALL=/bin/

This allows the user to /bin/cp which means he can replace any file under /bin/

# Solution

Enumerate allowed commands lists with full paths (and hashes) 


# Rule 2

Dont use wildcards, its hard to predict the effect of using them:

   foo ALL = /usr/bin/cat /var/log/dmesg*

This will actuall allow access to any file to be tailed:

   sudo cat /var/log/dmesg /etc/passwd

# Solution

Enumerate the entire set of allowed args into the command:

   foo ALL = /usr/bin/cat /var/log/dmesg, /usr/bin/cat /var/log/dmesg.0

We will use grant in order to generat that list for us:

   grant cmnd-alias --cmd CAT_LOGS /usr/bin/cat  --args /var/log/dmesg.{\d}

Grant will expand the list for us and create an aliase that we can use and validate:

    Cmnd_Alias CAT_LOGS = /usr/bin/cat /var/log/dmesg, /usr/bin/cat /var/log/dmesg.0


# Rule 3
Dont allow ALL for cmnd aliases in user spec

   foo ALL = NOPASSWD:EXEC ALL

# Solution

Enumerate the entire set of allowed commands and args using grant:

   grant cmnd-alias --cmd CAT_LOGS /usr/bin/cat  --args /var/log/dmesg.{\d}

Or use a spec file to pass in allow commands and args:

   grant cmnd-alias --cmnd-spec commands.edn


# Rule 4
Never use negation in command lists:


     Cmnd_Alias BLACKLIST = /usr/bin/sudo, /usr/bin/bash, /usr/bin/sh

     foo ALL = ALL, !BLACKLIST

The user can always copy commands around making them useless:

    sudo cp my-evil /usr/bin/sudo

Solution:


Enumerate the entire set of allowed commands and args using grant, enable checksums for whitelisted commands (copying over them won't work):

   grant cmnd-alias --cmd CAT_LOGS /usr/bin/cat  --args /var/log/dmesg.{\d} --checksum


# Rule 5
Watch out for pager tools (like less) since they allow shell escapes:

   foo ALL = /usr/bin/less

The user can now run:

   sudo less /tmp/foo

And from within less run:

   !visudo

Solution:

Set NOEXEC globally and limit EXEC to users running commands you trust:

    Defaults NOEXEC

    root = EXEC: /usr/bin/visudo

NOEXEC can limit only the load of shared libraries, static binaries are not effected so make sure to not allow compilers or software installer:

    gcc main.c -o EvilMe

Or:

    sudo apt install my-evil-thing


Rule 6:

Done allow non trusted users to run sudoer on folders no owned by root:

    foo ALL = ALL: /home/foo/bin/

The user can now add and run any binary as root:

   cd /home/foo/bin/
   gcc main.c -o EvilMe
   sudo /home/foo/bin/my-evil-bin

Solution:

Scan existing sudoers files with grant to detect such cases:

   grant analyse --input /etc/sudoers.d/foo

Allow only verified binaries to run from root owned libraies
