def add_copyright():

    import os
    import sys

    project = str(sys.argv[1])
    file_type = str(sys.argv[2])
    print(project)
    print(file_type)

    # Create an array of paths mirroring structure of current directory
    # while ignoring this script and problematic file types
    paths = []
    for (root, dirs, files) in os.walk('.', topdown=True):
        for f in files:
            if f == 'add-copyright.py':
                continue
            elif '.data' in f:
                continue
            elif '.png' in f:
                continue
            elif '.jar' in f:
                continue
            elif '.' not in f:
                continue
            elif '.pack' in f:
                continue
            elif '.idx' in f:
                continue
            else:
                path = os.path.join(root, f)
                paths.append(path)
    # print(paths)

    # Iterate through file paths, adding a copyright line to each file
    for p in paths:
        if '.circleci' in p:
            continue
        elif '.github' in p:
            continue 
        elif '.gitignore' in p:
            continue
        else:        
            if file_type == 'java':
                if p[-4:] == 'java':
                    with open(p, 'r') as t:
                        contents = t.readlines()
                        line = f'/* Copyright 2018-2022 contributors to the {project} project */\n\n'
                        contents.insert(0, line)
                    with open(p, 'w') as t:
                        contents = ''.join(contents)
                        t.write(contents)
            elif file_type == 'py':
                if p[-2:] == 'py':
                    with open(p, 'r') as t:
                        contents = t.readlines()
                        line = f'# Copyright 2018-2022 contributors to the {project} project\n\n'
                        contents.insert(0, line)
                    with open(p, 'w') as t:
                        contents = ''.join(contents)
                        t.write(contents)
            elif file_type == 'md':
                if p[-2:] == 'md':
                    with open(p, 'r') as t:
                        contents = t.readlines()
                        line = f'<!-- Copyright 2018-2022 contributors to the {project} project -->\n\n'
                        contents.insert(0, line)
                    with open(p, 'w') as t:
                        contents = ''.join(contents)
                        t.write(contents)
            elif file_type == 'html':
                if p[-2:] == 'html':
                    with open(p, 'r') as t:
                        contents = t.readlines()
                        line = f'<!-- Copyright 2018-2022 contributors to the {project} project -->\n\n'
                        contents.insert(0, line)
                    with open(p, 'w') as t:
                        contents = ''.join(contents)
                        t.write(contents)
            elif file_type == 'txt':
                if p[-3:] == 'txt':
                    with open(p, 'r') as t:
                        contents = t.readlines()
                        line = f'Copyright 2018-2022 contributors to the {project} project\n\n'
                        contents.insert(0, line)
                    with open(p, 'w') as t:
                        contents = ''.join(contents)
                        t.write(contents)
            elif file_type == 'rs':
                if p[-2:] == 'rs':
                    with open(p, 'r') as t:
                        contents = t.readlines()
                        line = f'// Copyright 2018-2022 contributors to the {project} project\n\n'
                        contents.insert(0, line)
                    with open(p, 'w') as t:
                        contents = ''.join(contents)
                        t.write(contents)
            elif file_type == 'sh':
                if p[-2:] == 'sh':
                    with open(p, 'a') as t:
                        line = f'\n# Copyright 2018-2022 contributors to the {project} project'
                        t.write(line)
            elif file_type == 'ts':
                if p[-2:] == 'ts':
                    with open(p, 'r') as t:
                        contents = t.readlines()
                        line = f'// Copyright 2018-2022 contributors to the {project} project\n\n'
                        contents.insert(0, line)
                    with open(p, 'w') as t:
                        contents = ''.join(contents)
                        t.write(contents)

add_copyright()