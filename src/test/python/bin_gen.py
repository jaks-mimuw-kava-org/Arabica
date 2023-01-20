#!/usr/bin/enb python3

import sys

if __name__ == '__main__':
    if len(sys.argv) < 2:
        file_name = '../resources/reader/test.bin'
    else:
        file_name = sys.argv[1]

    with open(file_name, 'wb') as f:
        f.write(b'i like bread\x00\x04\xff\xb6java is the best')
