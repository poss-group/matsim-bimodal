import numpy as np
import pandas as pd
import os
import re
from HelperFunctions import *
import io
from numba import njit

@njit
def sortOutIdle(ts, vals, transit_interval):
    count = 0
    last_val = 0
    last_t = 0
    last_i = -1
    to_fill = False
    for i, t in enumerate(ts):
        if np.isnan(vals[i]):
            to_fill = True
        else:
            if to_fill == True:
                if t - last_t < transit_interval:
                    vals[last_i + 1 : i] = last_val
            to_fill = False
            last_val = vals[i]
            last_t = t
            last_i = i

    return vals

def sortOutIdlePd(series, transit_interval):
    new_vals = sortOutIdle(series.index.values, series.values, transit_interval)
    return pd.Series(data=new_vals, index=series.index)

def getAverageTimeSeries(series):
    time_diff = series.index.values[1:] - series.index.values[:-1]
    return (time_diff * series.values[:-1]).sum() / time_diff.sum()


def getPtOccupancies(path, transit_interval):
    file_data = open(path)
    data = ""
    pattern = re.compile("^time.*\t(\d+\.\d+) veh.*(tr_\d+_\d+).*Passenger.*?(\d+).*")
    #     count = 0
    for x in file_data:
        if x.startswith("time"):
            match = pattern.search(x)
            if match:
                #                 print(match.group(0))
                data += (
                    match.group(1) + ";" + match.group(2) + ";" + match.group(3) + "\n"
                )
    #                 count += 1
    #                 if count > 10000:
    #                     break
    file_data.close()

    occupancies = pd.read_csv(
        io.StringIO(data), names=["time", "transporter", "passengers"], sep=";"
    )
    occupancies = occupancies.sort_values(by=["transporter", "time"])

    occupancies = occupancies.pivot_table(
        index="time", columns="transporter", values="passengers", aggfunc="last"
    )
    #     new_first_col = pd.DataFrame([[0] * len(occupancies.columns)], columns=occupancies.columns)
    #     occupancies = new_first_col.append(occupancies).fillna(method='ffill')
    occupancies = occupancies.apply(lambda x: sortOutIdlePd(x, transit_interval))

    #     with pd.option_context('display.max_rows', None, 'display.max_columns', None):
    #         display(occupancies)
    average_occupancies = occupancies.sum(axis=1) / occupancies.count(axis=1)
    return average_occupancies