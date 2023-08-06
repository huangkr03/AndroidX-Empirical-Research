from xml.etree import ElementTree
import csv

if __name__ == '__main__':
    with open('migration.xml', encoding='utf8') as file:
        tree = ElementTree.parse(file)
    migration = {'CLASS': {}, 'PACKAGE': {}}
    root = tree.getroot()
    for child in root:
        if child.tag == 'migrate':
            migration[child.attrib['type']][child.attrib['old-name']] = child.attrib['new-name']
        # print(child.tag, child.attrib)

    print('done')

    with open('androidx-class-mapping.csv', encoding='utf8') as file:
        reader = csv.reader(file)
        next(reader)
        official_mapping = {row[0]: row[1] for row in reader}

    # rows = []
    # difference between migration.xml and androidx-class-mapping.csv
    rows = []
    for cl in migration['CLASS']:
        if cl not in official_mapping:
            # missing class mapping in androidx-class-mapping.csv
            print(f"{cl},{migration['CLASS'][cl]},None")
            rows.append([cl, migration['CLASS'][cl], None])
        if cl in official_mapping and official_mapping[cl] != migration['CLASS'][cl]:
            # class mapping is different
            print(f"{cl},{migration['CLASS'][cl]},{official_mapping[cl]}")
            rows.append([cl, migration['CLASS'][cl], official_mapping[cl]])

    for cl in official_mapping:
        if cl not in migration['CLASS']:
            # check if it is a package
            sp = cl.split('.')
            current_package_name = cl
            for i in range(1, len(sp) - 1):
                current_package_name = current_package_name.rsplit('.', 1)[0]
                if current_package_name in migration['PACKAGE']:
                    break
            else:
                # missing class mapping in migration.xml
                print(f"{cl},None,{official_mapping[cl]}")
                rows.append([cl, None, official_mapping[cl]])

    # sorted(rows, key=lambda x: x[0])

    with open('WEB&IDE-mapping-diff.csv', 'w', encoding='utf8', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(['Support Class', 'IDE mapping', 'WEB mapping'])
        writer.writerows(rows)


    package_mapping = {}
    for cl in migration['PACKAGE']:
        package_mapping[cl] = migration['PACKAGE'][cl]

    # for m in official_mapping:
    #     sp = m.split('.')
    #     current_package_name = m
    #     for i in range(1, len(sp) - 1):
    #         current_package_name = current_package_name.rsplit('.', 1)[0]
    #         if current_package_name in package_mapping:
    #             c1 = package_mapping[current_package_name]
    #             c2 = official_mapping[m]
    #             if m not in migration['CLASS'] and package_mapping[current_package_name] not in official_mapping[m]:
    #                 print(f"{m}, {package_mapping[current_package_name]}, {official_mapping[m]}")
    #                 break
    #             else:
    #                 break

    # for m in official_mapping:
    #     sp = m.split('.')
    #     if m in migration['CLASS']:
    #         continue
    #     current_package_name = m
    #     for i in range(1, len(sp) - 1):
    #         current_package_name = current_package_name.rsplit('.', 1)[0]
    #         if current_package_name in package_mapping:
    #             break
    #     else:
    #         print(f"{m}, {official_mapping[m]}")

    print('done')


