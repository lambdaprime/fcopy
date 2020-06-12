**fcopy** - utility to copy files. Before copying the file **fcopy** finds its total size and requests OS to allocate free space for a new copy in advance. After space is allocated **fcopy** starts multiple worker threads which read data from the source file in blocks of certain size and write them to the destination file. 

At that moment **fcopy** does not suport data integrity verification after the files were copied. Please use md5sum/crcsum if needed. **fcopy** relies on Java IO and if it does not throw an exception then **fcopy** considers data as being written to the disk without any corruptions.

lambdaprime <id.blackmesa@gmail.com>

# Download

You can download **fcopy** from <https://github.com/lambdaprime/fcopy/blob/master/fcopy/release>

# Requirements

Java 11

# Usage

```
fcopy [ OPTION ]... SRC DST
```

Where:

SRC - source which may be either file or directory. In case of directory **fcopy** will copy all content of it recursively

DST - destination which may be either file or directory depending from the SRC you use

Options:

-bs BLOCK_SIZE - when **fcopy** reads and writes data it does it in blocks of given size in bytes (default is 256K as the most popular among SSD drives)

-t NUMBER_OF_THREADS - number of worker threads which will copy the data in parallel (default is number of cores in the system)

-c QUEUE_CAPACITY - size of internal queue from which worker threads pick up the blocks they need to copy (default is NUMBER_OF_THREADS^2)

-s - enables silent mode

# Examples

Copy ~/test file to /tmp directory:

``` bash
% fcopy ~/test /tmp
Configuration:
Threads: 11
Queue size: 121
Block size: 256000

Copying /home/ubuntu/test to /tmp/test
```

# Measurements

Below are the measurements between fcopy, rsync and cp.

## Single file

Test files:
- ubuntu-18.04.1-desktop-amd64.iso 1.9G (1953349632)

### fcopy

Default:

```bash
% time -p fcopy ubuntu-18.04.1-desktop-amd64.iso /tmp/l
Configuration:
Threads: 11
Queue size: 121
Block size: 256000

Copying ubuntu-18.04.1-desktop-amd64.iso to /tmp/l
real 4.24
user 2.07
sys 6.81
```

With 1MB block size:

```bash
% time -p fcopy -bs 1000000 ubuntu-18.04.1-desktop-amd64.iso /tmp/l
Configuration:
Threads: 11
Queue size: 121
Block size: 1000000

Copying ubuntu-18.04.1-desktop-amd64.iso to /tmp/l
real 2.53
user 1.58
sys 4.94
```

With 30 threads and 2048000 block size:

```bash
% time -p fcopy -bs 2048000 -t 30 ubuntu-18.04.1-desktop-amd64.iso /tmp/l
Configuration:
Threads: 30
Queue size: 121
Block size: 2048000

Copying ubuntu-18.04.1-desktop-amd64.iso to /tmp/l
real 1.89
user 1.11
sys 3.42
```

### rsync

```bash
% time -p rsync -W ubuntu-18.04.1-desktop-amd64.iso /tmp/l
real 4.35
user 5.50
sys 1.85
```

### cp

```bash
% time -p cp ubuntu-18.04.1-desktop-amd64.iso /tmp/l
real 1.43
user 0.02
sys 1.12
```

## Multiple files

Test folder:
- lubuntu-18.04-alternate-amd64.iso 717M (751828992)
- lubuntu-19.04-desktop-amd64.iso 1.6G (1657700352)
- ubuntu-18.04.1-desktop-amd64.iso 1.9G (1953349632)
- ubuntu-18.04.1-server-amd64.iso 715M (749731840)
- ubuntu-19.10-server-amd64.iso 785M (823132160)

### fcopy

Default;

``` bash
% time -p fcopy ubuntu /tmp/l
Configuration:
Threads: 11
Queue size: 121
Block size: 256000

Copying ubuntu/lubuntu-18.04-alternate-amd64.iso to /tmp/l/lubuntu-18.04-alternate-amd64.iso
Copying ubuntu/lubuntu-19.04-desktop-amd64.iso to /tmp/l/lubuntu-19.04-desktop-amd64.iso
Copying ubuntu/ubuntu-18.04.1-desktop-amd64.iso to /tmp/l/ubuntu-18.04.1-desktop-amd64.iso
Copying ubuntu/ubuntu-18.04.1-server-amd64.iso to /tmp/l/ubuntu-18.04.1-server-amd64.iso
Copying ubuntu/ubuntu-19.10-server-amd64.iso to /tmp/l/ubuntu-19.10-server-amd64.iso
real 21.61
user 5.40
sys 31.32
```

With 2048000 block size:

``` bash
% time -p fcopy -bs 2048000 ubuntu /tmp/l
Configuration:
Threads: 11
Queue size: 121
Block size: 2048000

Copying ubuntu/lubuntu-18.04-alternate-amd64.iso to /tmp/l/lubuntu-18.04-alternate-amd64.iso
Copying ubuntu/lubuntu-19.04-desktop-amd64.iso to /tmp/l/lubuntu-19.04-desktop-amd64.iso
Copying ubuntu/ubuntu-18.04.1-desktop-amd64.iso to /tmp/l/ubuntu-18.04.1-desktop-amd64.iso
Copying ubuntu/ubuntu-18.04.1-server-amd64.iso to /tmp/l/ubuntu-18.04.1-server-amd64.iso
Copying ubuntu/ubuntu-19.10-server-amd64.iso to /tmp/l/ubuntu-19.10-server-amd64.iso
real 8.08
user 4.25
sys 10.87
```

With 4096000 block size:

``` bash
% time -p fcopy -bs 4096000 ubuntu /tmp/l 
Configuration:
Threads: 11
Queue size: 121
Block size: 4096000

Copying ubuntu/lubuntu-18.04-alternate-amd64.iso to /tmp/l/lubuntu-18.04-alternate-amd64.iso
Copying ubuntu/lubuntu-19.04-desktop-amd64.iso to /tmp/l/lubuntu-19.04-desktop-amd64.iso
Copying ubuntu/ubuntu-18.04.1-desktop-amd64.iso to /tmp/l/ubuntu-18.04.1-desktop-amd64.iso
Copying ubuntu/ubuntu-18.04.1-server-amd64.iso to /tmp/l/ubuntu-18.04.1-server-amd64.iso
Copying ubuntu/ubuntu-19.10-server-amd64.iso to /tmp/l/ubuntu-19.10-server-amd64.iso
real 7.41
user 4.07
sys 9.34
```

### rsync

``` bash
% time -p rsync -rW ubuntu /tmp/l
real 14.31
user 17.36
sys 5.72
```

``` bash
% time -p rsync -r ubuntu /tmp/l 
real 13.93
user 17.62
sys 5.02
```

### cp

``` bash
% time -p cp -rf ubuntu /tmp/l
real 5.87
user 0.04
sys 3.55
```
