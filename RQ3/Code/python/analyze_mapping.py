import csv
import json

fdroid_bundled_classes = {}
gplay_bundled_classes = {}
fdroid_reached_classes = {}
gplay_reached_classes = {}
try_mapping = {}
try_mapping_support = {}
try_mapping_x = {}
already_mapping = {}
already_mapping_support = {}
already_mapping_x = {}
no_mapping_support = {}
no_official_mapping = {}
androidx_classes_set = None
support_classes_set = None

def init():
    global fdroid_bundled_classes, gplay_bundled_classes, fdroid_reached_classes, gplay_reached_classes
    with open('./fdroid_bundled_classes.txt', 'r', encoding='utf8') as file:
        lines = [line.rstrip('\n') for line in file.readlines()]
        for line in lines:
            tmp = line.split(': ')
            fdroid_bundled_classes[tmp[0]] = int(tmp[1])
    with open('./gplay_bundled_classes.txt', 'r', encoding='utf8') as file:
        lines = [line.rstrip('\n') for line in file.readlines()]
        for line in lines:
            tmp = line.split(': ')
            gplay_bundled_classes[tmp[0]] = int(tmp[1])
    with open('./fdroid_reached_classes.txt', 'r', encoding='utf8') as file:
        lines = [line.rstrip('\n') for line in file.readlines()]
        for line in lines:
            tmp = line.split(': ')
            fdroid_reached_classes[tmp[0]] = int(tmp[1])
    with open('./gplay_reached_classes.txt', 'r', encoding='utf8') as file:
        lines = [line.rstrip('\n') for line in file.readlines()]
        for line in lines:
            tmp = line.split(': ')
            gplay_reached_classes[tmp[0]] = int(tmp[1])

    # open csv file
    with open('Try Mapping.csv') as csvfile:
        reader = csv.reader(csvfile)
        next(reader)
        for row in reader:
            try_mapping[row[0]] = row[1]
            try_mapping_support[row[0]] = 0
            try_mapping_x[row[1]] = 0

    with open('androidx-class-mapping.csv') as csvfile:
        reader = csv.reader(csvfile)
        next(reader)
        for row in reader:
            already_mapping[row[0]] = row[1]
            already_mapping_support[row[0]] = 0
            already_mapping_x[row[1]] = 0

    # get class name
    androidx_classes = []
    support_classes = []
    with open('androidx_classes.txt', 'r') as file:
        lines = [line.rstrip('\n') for line in file.readlines()]
        for line in lines:
            androidx_classes.append(line)
    with open('support_classes.txt', 'r') as file:
        lines = [line.rstrip('\n') for line in file.readlines()]
        for line in lines:
            support_classes.append(line)

    global androidx_classes_set, support_classes_set
    androidx_classes_set = set(androidx_classes)
    support_classes_set = set(support_classes)

def analysis():
    global try_mapping_x, already_mapping_x, try_mapping_support, already_mapping_support, no_mapping_support, no_official_mapping
    for key in gplay_reached_classes:
        if key not in androidx_classes_set and key not in support_classes_set:
            continue
        if key in already_mapping_support:
            already_mapping_support[key] += gplay_reached_classes[key]
        elif key in already_mapping_x:
            already_mapping_x[key] += gplay_reached_classes[key]
        else:
            if key in support_classes_set:
                if key in no_official_mapping:
                    no_official_mapping[key] += gplay_reached_classes[key]
                else:
                    no_official_mapping[key] = gplay_reached_classes[key]
            if key in try_mapping_support:
                try_mapping_support[key] += gplay_reached_classes[key]
            elif key in try_mapping_x:
                try_mapping_x[key] += gplay_reached_classes[key]
            elif key.startswith('android.support'):
                no_mapping_support[key] = gplay_reached_classes[key]
    # for key in fdroid_reached_classes:
    #     if key not in androidx_classes_set and key not in support_classes_set:
    #         continue
    #     if key in try_mapping_support:
    #         try_mapping_support[key] += fdroid_reached_classes[key]
    #     elif key in try_mapping_x:
    #         try_mapping_x[key] += fdroid_reached_classes[key]
    #     elif key in already_mapping_support:
    #         already_mapping_support[key] += fdroid_reached_classes[key]
    #     elif key in already_mapping_x:
    #         already_mapping_x[key] += fdroid_reached_classes[key]
    #     elif key.startswith('android.support'):
    #         if key in no_mapping_support:
    #             no_mapping_support[key] += fdroid_reached_classes[key]
    #         else:
    #             no_mapping_support[key] = fdroid_reached_classes[key]


    # sort by value
    try_mapping_support = {k: v for k, v in sorted(try_mapping_support.items(), key=lambda item: item[1], reverse=True)}
    try_mapping_x = {k: v for k, v in sorted(try_mapping_x.items(), key=lambda item: item[1], reverse=True)}
    already_mapping_support = {k: v for k, v in sorted(already_mapping_support.items(), key=lambda item: item[1], reverse=True)}
    already_mapping_x = {k: v for k, v in sorted(already_mapping_x.items(), key=lambda item: item[1], reverse=True)}
    no_mapping_support = {k: v for k, v in sorted(no_mapping_support.items(), key=lambda item: item[1], reverse=True)}
    no_official_mapping = {k: v for k, v in sorted(no_official_mapping.items(), key=lambda item: item[1], reverse=True)}

    # save these all into txt file
    with open('result/try_mapping_support.txt', 'w') as file:
        for key in try_mapping_support:
            file.write(key + ': ' + str(try_mapping_support[key]) + '\n')
    with open('result/try_mapping_x.txt', 'w') as file:
        for key in try_mapping_x:
            file.write(key + ': ' + str(try_mapping_x[key]) + '\n')
    with open('result/already_mapping_support.txt', 'w') as file:
        for key in already_mapping_support:
            file.write(key + ': ' + str(already_mapping_support[key]) + '\n')
    with open('result/already_mapping_x.txt', 'w') as file:
        for key in already_mapping_x:
            file.write(key + ': ' + str(already_mapping_x[key]) + '\n')
    with open('result/no_mapping_support.txt', 'w') as file:
        for key in no_mapping_support:
            file.write(key + ': ' + str(no_mapping_support[key]) + '\n')
    with open('result/no_official_mapping.txt', 'w') as file:
        for key in no_official_mapping:
            file.write(key + ': ' + str(no_official_mapping[key]) + '\n')


def get_class_name():
    with open('androidx.json') as file:
        androidx = json.load(file)
    with open('support.json') as file:
        support = json.load(file)

    androidx_classes = []
    support_classes = []
    for class_name in androidx['bundledClasses']:
        if '$' not in class_name['className']:
            androidx_classes.append(class_name['className'])
    for class_name in support['bundledClasses']:
        if '$' not in class_name['className']:
            support_classes.append(class_name['className'])

    androidx_classes = sorted(androidx_classes)
    support_classes = sorted(support_classes)
    with open('androidx_classes.txt', 'w') as file:
        for class_name in androidx_classes:
            file.write(class_name + '\n')
    with open('support_classes.txt', 'w') as file:
        for class_name in support_classes:
            file.write(class_name + '\n')


if __name__ == '__main__':
    init()
    analysis()

    print("done")
