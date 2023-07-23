

if __name__ == '__main__':
    mapping = {}
    with open('../androidx-class-mapping.csv', encoding='utf8') as file:
        lines = file.readlines()
        for line in lines:
            line_split = line.strip('\n').split(',')
            mapping[line_split[0]] = line_split[1]

    value_set = mapping.values()
    final_lines = []
    with open('gplay_reached_classes.txt', encoding='utf8') as file:
        lines = file.readlines()
        for line in lines:
            line_split = line.strip('\n').split(': ')
            if len(line_split[0].split('.')[-1]) <= 1:
                continue
            if line_split[0] not in value_set and not line_split[0].startswith('android.support.v4.media'):
                final_lines.append(line)

    with open('gplay_reached_classes_filtered.txt', 'w', encoding='utf8') as file:
        file.writelines(final_lines)