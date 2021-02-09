import numpy as np
import matplotlib.pyplot as plt
from statistics import mean
from operator import sub, add
from functools import reduce

input_folder = 'generated'
input_files = ['simple_leaf_count', 'simple_leaf_count_foresting_no_inner_pruning', 'foresting_leaf_count', 'inner_count_foresting', 'inner_count_foresting_no_inner_pruning', 'ratio', 'ratio_no_inner_pruning', 'ratio_foresting', 'ratio_foresting_no_inner_pruning', 'diff_foresting', 'ratio_pruning_5_foresting', 'ratio_pruning_2_foresting', 'ratio_pruning_0_foresting', 'random_hierarchy', 'send_direct', 'optimal']
colors = ['b', 'g', 'r', 'c', 'm', 'k']

def parse_file(input_file):
    data = {}
    size_set = []
    with open(input_folder + '/' + input_file + '.txt', 'r') as file:
        size = None
        for line in file.readlines():
            line = line.strip()

            if not line.startswith('['):
                if size != None:
                    data[size] = size_set
                    size_set = []
                size = int(line)
            else:
                size_set.append([int(i) for i in line[1:-1].split(', ')])
        data[size] = size_set

    return data

def boxplot(data_list, ax, key):
    box_data = []
    for data in data_list:
        #box_data.append(list(map(lambda x: sum(x)/len(x), data[key])))
        costs = list(reduce(add, data[key]))
        box_data.append(costs)
        print(sum(costs)/len(costs))

    ax.boxplot(box_data)

def boxplot_multiple_files(input_files, ax, key):
    boxplot([parse_file(input_file) for input_file in input_files], ax, key)

def average_of(reduce_func):
    return lambda list: mean(map(reduce_func, list))

def min_max_line(data, ax, color):
    keys = data.keys()
    x = list(keys)
    values = list(map(lambda x: data[x], keys))
    y_min = list(map(average_of(min), values))
    y_max = list(map(average_of(max), values))


    ax.plot(x, y_min, color=color)
    ax.plot(x, y_max, color=color)

def min_max_line_multiple_files(input_files, ax):
    for input_file, color in zip(input_files, colors):
        print(color, ': ', input_file)
        min_max_line(parse_file(input_file), ax, color)

def distribution(start, end, input_file, ax, key):
    occurences = [0]*(end + 1)
    total = 0
    for i in parse_file(input_file)[key]:
        for j in i:
            occurences[j] += 1
            total += 1

    for i in range(len(occurences)):
        occurences[i] = sum(occurences[i:])/total
    occurences = occurences[start:end+1]

    ax.plot(list(range(start, end + 1)), occurences)

def min_max_occuring_value(input_files, key):
    min = float('inf')
    max = 0
    for input_file in input_files:
        for i in parse_file(input_file)[key]:
            for j in i:
                if j > max:
                    max = j
                if j < min:
                    min = j
    return (min, max)

fig, ax = plt.subplots()

#### box plot
#ax.set_xticklabels(input_files)
#ax.yaxis.grid(True)
#boxplot_multiple_files(input_files, plt, 4)

### distribution

files = ['ratio', 'ratio_foresting_one_shot', 'ratio_foresting']
key = 128
min, max = min_max_occuring_value(files, key)
min = 118
for file in files:
    distribution(min, max, file, plt, key)

plt.legend(files)

plt.show()

def count_losers():
    simple = parse_file('simple_leaf_count')
    foresting = parse_file('foresting_leaf_count')

    foresting_score = 0
    simple_score = 0
    total = 0

    def max_diff(x, y):
        return max(x) - max(y)

    for key in simple.keys():
        for diff in map(max_diff, foresting[key], simple[key]):
            total += 1
            if diff > 0:
                foresting_score += 1
            elif diff < 0:
                simple_score += 1

    print(total)
    print(foresting_score)
    print(simple_score)
