import json

if __name__ == '__main__':
    mapping = {}
    with open('androidx-class-mapping.csv', encoding='utf8') as file:
        lines = file.readlines()
        for line in lines:
            line_split = line.strip('\n').split(',')
            mapping[line_split[0]] = line_split[1]
    with open('dependency_map.json', encoding='utf8') as file:
        dependency_map = json.load(file)

    dependencies = {}
    androidx_classes_with_dependencies_number = 0
    support_classes_with_dependencies = set()
    for key in dependency_map:
        temp_dep = 0
        for item in dependency_map[key]:
            dependencies_ = []
            if item not in mapping:
                temp_dep = 1
                dependencies_.append(item)
                support_classes_with_dependencies.add(item)
            if dependencies_:
                dependencies[key] = dependencies_
        androidx_classes_with_dependencies_number += temp_dep

    print(len(support_classes_with_dependencies))
    print(len(dependencies))
    with open('dependencies_filtered.json', 'w', encoding='utf8') as file:
        json.dump(dependencies, file, indent=4)