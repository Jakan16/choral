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

    print(sum(occurences))

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

def plot_average(ax, input_files, key, x_values):
    y_values = []
    for input_file in input_files:
        total = 0
        sum = 0
        for i in parse_file(input_file)[key]:
            for j in i:
                total += 1
                sum += j
        y_values.append(sum/total)
    ax.plot(x_values, y_values)

#### box plot
#files = ['leaf_count_no_inner_pruning', 'inner_count_no_inner_pruning', 'ratio_no_inner_pruning', 'random_hierarchy',  'send_direct', 'optimal']
#ticks = ['leaf count', 'inner count', 'ratio', 'random', 'direct', 'optimal']
#files = ['leaf_count_no_inner_pruning', 'leaf_count_foresting_one_shot_no_inner_pruning', 'simple_leaf_count_foresting_no_inner_pruning', 'inner_count_no_inner_pruning', 'inner_count_foresting_one_shot_no_inner_pruning', 'inner_count_foresting_no_inner_pruning', 'ratio_no_inner_pruning', 'ratio_foresting_one_shot_no_inner_pruning', 'ratio_foresting_no_inner_pruning']
#ticks = ['leaf', 'leaf f', 'leaf fi', 'inner', 'inner f', 'inner fi', 'ratio', 'ratio f', 'ratio fi']
#files = ['inner_count_no_inner_pruning', 'inner_count', 'ratio_no_inner_pruning', 'ratio']
#ticks = ['inner', 'inner ip', 'ratio', 'ratio ip']
#files = ['leaf_count_no_inner_pruning', 'inner_count', 'inner_count_foresting', 'ratio', 'ratio_foresting']
#ticks = ['leaf', 'inner ip', 'inner ip fi', 'ratio ip', 'ratio ip fi']
#files = ['leaf_count_no_inner_pruning', 'leaf_count_pruning_5_foresting', 'leaf_count_pruning_0_foresting', 'ratio_foresting', 'ratio_pruning_5_foresting', 'ratio_pruning_0_foresting']
#ticks = ['leaf p1', 'leaf p.5', 'leaf p0', 'ratio p1', 'ratio p.5', 'ratio p0']
#ax.set_xticklabels(ticks)
#ax.yaxis.grid(True)
#boxplot_multiple_files(files, plt, 64)

### distribution

#files = ['ratio', 'ratio_foresting_one_shot', 'ratio_foresting', 'simple_leaf_count', 'leaf_count_foresting', 'foresting_leaf_count']
files = ['send_direct', 'simple_leaf_count', 'leaf_pf_8_one_shot', 'leaf_pf_8', 'ratio_pf_8_one_shot', 'ratio_pf_8']
key = 128
min, max = min_max_occuring_value(files, key)
min = 116
for file in files:
    distribution(min, max, file, plt, key)

#plt.legend(['ratio', 'ratio f', 'ratio fi', 'leaf', 'leaf f', 'leaf fi'])
ax.yaxis.grid(True)
plt.legend(['direct', 'simple leaf count', 'leaf f', 'leaf fi', 'ratio f', 'ratio fi'])

### cost vs pf
#files = ['leaf_pf_0', 'leaf_pf_1', 'leaf_pf_2', 'leaf_pf_3', 'leaf_pf_4', 'leaf_pf_5', 'leaf_pf_6', 'leaf_pf_7', 'leaf_pf_8', 'leaf_pf_9', 'leaf_pf_10']
#plot_average(plt, files, 64, [i/10 for i in range(11)])
#files = ['leaf_pf_1', 'leaf_pf_2', 'leaf_pf_3', 'leaf_pf_e', 'leaf_pf_4', 'leaf_pf_5', 'leaf_pf_6', 'leaf_pf_7', 'leaf_pf_8', 'leaf_pf_9', 'leaf_pf_99', 'leaf_pf_10']
#plot_average(plt, files, 64, [0.1, 0.2, 0.3, 0.36787944117, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.99, 1])
#files = ['ratio_pf_1', 'ratio_pf_2', 'ratio_pf_3', 'ratio_pf_e', 'ratio_pf_4', 'ratio_pf_5', 'ratio_pf_6', 'ratio_pf_7', 'ratio_pf_8', 'ratio_pf_9', 'ratio_pf_99', 'ratio_pf_10']
#plot_average(plt, files, 64, [0.1, 0.2, 0.3, 0.36787944117, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.99, 1])
#plt.legend(['leaf', 'ratio'])
#ax.yaxis.grid(True)


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
