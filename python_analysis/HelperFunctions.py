import numpy as np

def timestmp2sec(date):
    secs = [3600, 60, 1]
    return sum([secs[i] * float(el) for i, el in enumerate(date.split(":"))])

def timestmphm2sec(date):
    secs = [3600, 60]
    return sum([secs[i] * float(el) for i, el in enumerate(date.split(":"))])

def sec2timestmp(seconds):
    secs = [3600, 60, 1]
    hours = int(seconds // secs[0])
    seconds = seconds % secs[0]
    mins = int(seconds // secs[1])
    seconds = seconds % secs[1]
    return str(hours) + ":" + str(mins) + ":" + str(int(seconds))

def combineModesSeriesStr(modesSeries):
    raw = "-".join(modesSeries).split("-")
    last = ""
    result = []
    for m in raw:
        if m != last:
            result.append(m)
            last = m
    return "-".join(result)

def getAverageOcc(df, exclude_empty_vehicles=False, count_idle_vehicles=False):
    if exclude_empty_vehicles == True and count_idle_vehicles == False:
        df.drop(columns=["STAY", "0 pax"], inplace=True)
        weights = np.arange(1, len(df.columns) + 1)
    elif count_idle_vehicles == False and exclude_empty_vehicles == False:
        df.drop(columns=["STAY"], inplace=True)
        weights = np.arange(0, len(df.columns))
    elif count_idle_vehicles == True and exclude_empty_vehicles == False:
        weights = np.zeros(len(df.columns))
        weights[1:] = np.arange(0, len(df.columns) - 1)
    else:
        raise Exception("Entered combination of modes not possible combination of modes")

    pass_sum = df.sum(axis=1)
    mean = (df.dot(weights) / pass_sum).mean()
    #var = ((df.dot(weights**2) / pass_sum)).mean() # Variance per timestep across occupancies
    var = ((df.dot(weights) / pass_sum)**2).mean() # Variance across observed time period
    return mean, np.sqrt(var-mean**2)

    # Equivalent to Variance per timestep across occupancies
    #s = (df.to_numpy() * (weights - mean) ** 2).sum(axis=1) / (pass_sum - 1)
    #return mean, np.sqrt(s).mean()

def getStandingFraction(df):
    pass_sum = df.sum(axis=1)
    standing_fraction = (df["STAY"] / pass_sum).mean()
    return standing_fraction
