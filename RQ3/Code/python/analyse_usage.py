import json
import os

import tqdm

gplay_bundled_classes = {}
gplay_reached_classes = {}
fdroid_bundled_classes = {}
fdroid_reached_classes = {}

location = "/home/12012705/android/compass/"
filtered_apk_fdroid = '/home/12012705/android/compass/fdroid_result/filtered_apk.txt'
filtered_apk_gplay = '/home/12012705/android/compass/gplay_result/filtered_apk.txt'

with open(filtered_apk_gplay, 'r') as f:
    gplay_apks = set([line.rstrip('.apk\n') for line in f.readlines()])
with open(filtered_apk_fdroid, 'r') as f:
    fdroid_apks = set([line.rstrip('.apk\n') for line in f.readlines()])

def gplay():
    # list all the files in ./gplay_result/result

    json_files = os.listdir(location + 'gplay_result/result')
    bar = tqdm.tqdm(total=len(json_files))
    for file in json_files:
        if file.rstrip('.json') not in gplay_apks:
            bar.update(1)
            continue
        with open(location + '/gplay_result/result/' + file, 'r') as f:
            json_file = json.load(f)
        bundled_classes = json_file['bundledClasses']
        for bundled_class in bundled_classes:
            bundled_class_name = bundled_class['className']
            if '$' in bundled_class_name:
                continue
            if bundled_class_name not in gplay_bundled_classes:
                gplay_bundled_classes[bundled_class_name] = 1
            else:
                gplay_bundled_classes[bundled_class_name] += 1

        reached_classes = json_file['reachedClasses']
        for reached_class in reached_classes:
            reached_class_name = reached_class['className']
            if '$' in reached_class_name:
                continue
            if reached_class_name not in gplay_reached_classes:
                gplay_reached_classes[reached_class_name] = 1
            else:
                gplay_reached_classes[reached_class_name] += 1
        bar.update(1)

def fdroid():
    # list all the files in ./fdroid_result/result

    json_files = os.listdir(location + 'fdroid_result/result')
    bar = tqdm.tqdm(total=len(json_files))
    for file in json_files:
        if file.rstrip('.json') not in fdroid_apks:
            bar.update(1)
            continue
        with open(location + 'fdroid_result/result/' + file, 'r') as f:
            json_file = json.load(f)
        bundled_classes = json_file['bundledClasses']
        for bundled_class in bundled_classes:
            bundled_class_name = bundled_class['className']
            if '$' in bundled_class_name:
                continue
            if bundled_class_name not in fdroid_bundled_classes:
                fdroid_bundled_classes[bundled_class_name] = 1
            else:
                fdroid_bundled_classes[bundled_class_name] += 1

        reached_classes = json_file['reachedClasses']
        for reached_class in reached_classes:
            reached_class_name = reached_class['className']
            if '$' in reached_class_name:
                continue
            if reached_class_name not in fdroid_reached_classes:
                fdroid_reached_classes[reached_class_name] = 1
            else:
                fdroid_reached_classes[reached_class_name] += 1
        bar.update(1)


def save_result():
    with open('gplay_bundled_classes_filtered.txt', 'w') as file:
        # sort the dictionary by value
        sorted_dict = sorted(gplay_bundled_classes.items(), key=lambda x: x[1], reverse=True)
        for item in sorted_dict:
            file.write(item[0] + ': ' + str(item[1]) + '\n')
    with open('gplay_reached_classes_filtered.txt', 'w') as file:
        # sort the dictionary by value
        sorted_dict = sorted(gplay_reached_classes.items(), key=lambda x: x[1], reverse=True)
        for item in sorted_dict:
            file.write(item[0] + ': ' + str(item[1]) + '\n')
    with open('fdroid_bundled_classes_filtered.txt', 'w') as file:
        # sort the dictionary by value
        sorted_dict = sorted(fdroid_bundled_classes.items(), key=lambda x: x[1], reverse=True)
        for item in sorted_dict:
            file.write(item[0] + ': ' + str(item[1]) + '\n')
    with open('fdroid_reached_classes_filtered.txt', 'w') as file:
        # sort the dictionary by value
        sorted_dict = sorted(fdroid_reached_classes.items(), key=lambda x: x[1], reverse=True)
        for item in sorted_dict:
            file.write(item[0] + ': ' + str(item[1]) + '\n')


if __name__ == '__main__':
    gplay()
    fdroid()
    save_result()
