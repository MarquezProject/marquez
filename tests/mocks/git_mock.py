from marquez_airflow.utils import execute_git


def execute_git_mock(*args, **kwargs):
    # just mock the git revision
    if len(args) == 2 and len(args[1]) > 0 and args[1][0] == 'rev-list':
        return 'abcd1234'
    return execute_git(*args, **kwargs)
