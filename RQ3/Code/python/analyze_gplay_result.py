import json
import os
import csv

import tqdm

gplay_bundled_classes = {}
gplay_reached_classes = {}
fdroid_bundled_classes = {}
fdroid_reached_classes = {}

location = "/home/12012705/android/compass/"
filtered_apk_fdroid = '/home/12012705/android/compass/fdroid_result/filtered_apk.txt'
with open(filtered_apk_fdroid, 'r') as f:
    fdroid_apks = set([line.rstrip('.apk\n') for line in f.readlines()])

def gplay():
    # list all the files in ./gplay_result/result

    json_files = os.listdir(location + 'gplay_result/result')
    bar = tqdm.tqdm(total=len(json_files))
    rows = [['apk', 'Support Class', 'Support Method', 'Support Field', 'AndroidX Class', 'AndroidX Method', 'AndroidX Field']]
    for file in json_files:
        support_classes = 0
        support_methods = 0
        support_fields = 0
        androidx_classes = 0
        androidx_methods = 0
        androidx_fields = 0
        with open(location + '/gplay_result/result/' + file, 'r') as f:
            json_file = json.load(f)
        reached_classes = json_file['reachedClasses']
        for reached_class in reached_classes:
            if reached_class['className'].startswith('android.support'):
                support_classes += 1
                support_methods += len(reached_class['methods'])
                support_fields += len(reached_class['fields'])
            elif reached_class['className'].startswith('androidx'):
                androidx_classes += 1
                androidx_methods += len(reached_class['methods'])
                androidx_fields += len(reached_class['fields'])

        rows.append([file.rstrip('.json'), support_classes, support_methods, support_fields, androidx_classes, androidx_methods, androidx_fields])
        bar.update(1)
    with open('result/gplay_result.csv', 'w') as f:
        writer = csv.writer(f)
        writer.writerows(rows)



if __name__ == '__main__':
    gplay()
