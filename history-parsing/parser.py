import requests
import os
import time
from datetime import datetime, timedelta

'''
<What it does>
Finding and logging data related to each bus arriving at the given stop(s).
Logs are in history.txt.
Useful for calculating delay: actual arrival time, scheduled arrival time

<When to run>
When you want to collect data for history.txt.
Old data in history.txt will not be overwritten, but some might be updated, which is intended,
because for the same trip at the same stop, we only want the most updated arrival time.

<How to run>
Run "python3 parsor.py".
Ctrl-C to quit. New data will not appear in history.txt until you Ctrl-C.
!!! DO NOT Ctrl-C when the last line in the terminal says "Working..." to avoid incomplete data
being logged. It is okay when it says "Sleeping...".

<Settings>
To change the stop(s), change stop_ids.
To change the log frequency, change log_frequency_in_minutes.

<Notes>
For brief explanation of how it does what it does, see main.
'''


curr_path = os.path.dirname(__file__)
key_relative_path = "../pgh-prt-truetime/key.secret"
schedule_relative_path = "../schedule-parsing/schedule.txt"
history_relative_path = "history.txt"
base_url = "https://truetime.portauthority.org/bustime/api/v3/getpredictions?format=json"
rtpidatafeed = "Port Authority Bus"

'''
Includes 10 popular bus stops in various Pittsburgh neighborhoods:
Oakland: 2565, 36
CMU: 7117, 4407
Squirrel Hill: 7126, 7096
Shadyside: 1177, 1167
East Liberty (Target, Trader Joe's): 19383, 3268
'''
stop_ids = [7117, 1177, 7126, 7096, 2565, 36, 4407, 1167, 19383, 3268]
log_frequency_in_minutes = 1
log_frequency_in_seconds = log_frequency_in_minutes * 60



def key():
    with open(os.path.join(curr_path, key_relative_path), "r") as f:
        return f.readlines()[0].strip()

def stpid():
    stpid = ""
    for i in range(len(stop_ids)):
        if i == 0:
            stpid += str(stop_ids[i])
        else:
            stpid += "," + str(stop_ids[i])
    return stpid

def write_first_line():
    with open(history_relative_path, "a") as history:
        history.write("tatripid,log_time,stop_name,stop_id,route_id,direction,scheduled_start_date,scheduled_start_time,actual_arrival_time,scheduled_arrival_time\n")

'''
Makes an API call and logs data related to the arrivals at the stops given. Actual arrival times
are logged by this method.
'''
def log_actual_arrivals(url_params):
    response = requests.get(url=base_url, params = url_params)
    data = response.json()

    try:
        arrivals = data["bustime-response"]["prd"]
    except: # no real-time arrivals at the given stops at the moment
        return

    with open(history_relative_path, "a") as history:
        for arrival in arrivals:
            scheduled_start_date, scheduled_start_time = convert_scheduled_start_date_time(arrival['stsd'], arrival['stst'])
            log_time = datetime.strptime(arrival['tmstmp'], '%Y%m%d %H:%M').strftime('%Y-%m-%dT%H:%M')
            history.write(f"{arrival['tatripid']},{log_time},'{arrival['stpnm']}',{arrival['stpid']},{arrival['rt']},{arrival['rtdir']},{scheduled_start_date},{scheduled_start_time},{arrival['prdtm'].split(' ')[1]}\n")

'''
For logged arrivals sharing the same trip id and stop id, keep only the most recent one and remove
the rest.
'''
def remove_earlier_duplicate_trips():
    seen_trip_stop_combos = set()
    unique_lines = []

    with open(history_relative_path, "r") as history:
        lines = reversed(history.readlines())

    for line in lines:
        try:
            lst = line.strip().split(",")
            trip_id = lst[0]
            stop_id = lst[3]
            scheduled_start_date = lst[6]
            if (trip_id, stop_id, scheduled_start_date) not in seen_trip_stop_combos:
                unique_lines.append(line)
                seen_trip_stop_combos.add((trip_id, stop_id, scheduled_start_date))

        # the first line is un-parseable
        except ValueError:
            unique_lines.append(line)

    unique_lines.reverse()

    with open(history_relative_path, "w") as history:
        history.writelines(unique_lines)

'''
Utility method to convert a date str and an int str that represents seconds past midnight to
date-time strs.
Example: "20231211 395" --> "2023-12-11" and "06:35"
Corner case handled: converts all "00:xx" to "24:xx" to be able to do schedule lookup, because
schedule.txt uses "24:xx" for "00:xx".
'''
def convert_scheduled_start_date_time(date_str, seconds_str):
    original_date = datetime.strptime(date_str, '%Y-%m-%d')
    date_time = original_date + timedelta(seconds=int(seconds_str))
    lst = date_time.strftime('%Y-%m-%d %H:%M').split(" ")
    date_str = lst[0]
    time_str = lst[1]
    if time_str.startswith("00:"):
        minute = time_str.split(":")[1]
        time_str = "24:" + minute
    return date_str, time_str

'''
Finds scheduled arrival time of a bus at a given stop, for a given route, whose departure from its
first stop is also given.
'''
def find_scheduled_arrival_time(stop_id, route_id, direction, scheduled_start_time):
    with open(os.path.join(curr_path, schedule_relative_path), "r") as schedule:
        lines = schedule.readlines()

    for i in range(1, len(lines)):
        line = lines[i].strip()
        stop = line.split(",")
        schedule_style_direction = "0" if direction == "OUTBOUND" else "1"
        if stop[1] == route_id and stop[3] == schedule_style_direction and stop[4].startswith(scheduled_start_time):
            if i == 1:
                # print(line.strip())
                break

            prev_line_route_id = lines[i - 1].split(",")[1]
            if prev_line_route_id != route_id:
                # print(line.strip())
                break

    for j in range(i + 1, len(lines)):
        line = lines[j].strip()
        stop = line.split(",")
        if stop[6] == stop_id:
            # print(line)
            try:
                return datetime.strptime(stop[4], '%H:%M:%S').strftime('%H:%M')
            except ValueError:
                timeArr = stop[4].split(':')
                timeStr = ":".join([str(int(timeArr[0]) - 24), timeArr[1], timeArr[2]])
                return datetime.strptime(timeStr, '%H:%M:%S').strftime('%H:%M')

'''
Logs found scheduled arrival times into history.txt by appending each to the end of their
corresponding arrivals.
'''
def log_scheduled_arrival_times():
    with open(history_relative_path, "r") as history:
        lines = history.readlines()

    new_lines = [lines[0]]

    for i in range(1, len(lines)):
        line = lines[i].strip()
        lst = line.split(",")

        if len(lst) == 10:
            new_lines.append(line + "\n")
            continue

        stop_id = lst[3]
        route_id = lst[4]
        direction = lst[5]
        scheduled_start_time = lst[7]
        scheduled_arrival_time = find_scheduled_arrival_time(stop_id, route_id, direction, scheduled_start_time)
        if scheduled_arrival_time != None:
            new_lines.append(line + f",{scheduled_arrival_time}\n")

    with open(history_relative_path, "w") as history:
        history.writelines(new_lines)

'''
Returns True if the current time is past the time specified by hr_input and min_input. Works like an
alarm.

Can use in main to throw an exception when the current time is past this time to stop the script
from running.

To use it, insert the following code right below the line where log_actual_arrivals(url_params)
is called in main to stop the script from running after, for example, 13:40.
if time_is_past(13, 40):
    throw KeyboardInterrupt
'''
def time_is_past(hr_input, min_input):
    alarm = datetime.now().replace(hour=hr_input, minute=min_input, second=0, microsecond=0)
    now = datetime.now()
    return now > alarm

if __name__ == "__main__":
    url_params = dict(
        key = key(),
        stpid = stpid(),
        rtpidatafeed = rtpidatafeed,
        tmres = "m"
    )

    try:
        if os.stat(history_relative_path).st_size == 0: # if history.txt is empty
            # write column names in the first line
            write_first_line()

        # keep logging before Ctrl-C is pressed
        while True:
            # logs actual arrivals at the given stop(s)
            # scheduled arrival time will be produced after exiting the loop by Ctrl-C
            print("Working...")
            log_actual_arrivals(url_params)
            print("Sleeping...")
            time.sleep(log_frequency_in_seconds)

    # Ctrl-C is pressed
    except KeyboardInterrupt:
        print("\nPlease wait...")
        # for logged arrivals sharing the same trip id and stop id, keep only the most recent one
        remove_earlier_duplicate_trips()
        # find matching scheduled arrival times from schedule.txt
        log_scheduled_arrival_times()
        print("Finished.")


