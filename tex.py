import pandas as pd

# df = pd.read_csv('table.csv', header=None, names=['label','c1','c2','c3','c4','c5'])
df = pd.read_clipboard(sep='&', header=None, names=['label','c1','c2','c3','c4','c5'])
df['c1'] = df['c1'].astype(float).round(4)
df[['c2','c3','c4']] = df[['c2','c3','c4']].astype(float).round(5)
df['c5'] = df['c5'].astype(float).astype(float).round(5)

def make_fmt(decimals):
    fmt = "{:." + str(decimals) + "f}"
    return lambda x: fmt.format(x).rstrip('0').rstrip('.')

formatters = {
    'c1': make_fmt(4),
    'c2': make_fmt(5),
    'c3': make_fmt(5),
    'c4': make_fmt(5),
    'c5': make_fmt(5),
    # 'c5': lambda x: str(int(x)),
}

print(r"\begin{table}[H]")
print(r"  \centering")
print(r"  \begin{tabular}{|l|r|r|r|r|r|}")
print(r"    \hline")
print(r"     Алгоритъм & Средна грешка & Минимална грешка & Параметър 1 & Параметър 2 & Параметър 3 \\")
for _, row in df.iterrows():
    print(r"    \hline")
    label = row['label'].strip()
    v1 = formatters['c1'](row['c1'])
    v2 = formatters['c2'](row['c2'])
    v3 = formatters['c3'](row['c3'])
    v4 = formatters['c4'](row['c4'])
    v5 = formatters['c5'](row['c5'])
    print(f"    \\textbf{{{label}}} & {v1} & {v2} & {v3} & {v4} & {v5} \\\\")

print(r"    \hline")
print(r"  \end{tabular}")
# print(r"\caption{Стойности за 300 кита и 30 итерацииf}")
print(r"\end{table}")