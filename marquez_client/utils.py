from marquez_client.models import DatasetFieldType, DatasetType


def make_field(name, data_type, description=None):
    if isinstance(data_type, str):
        if not DatasetFieldType.__members__.__contains__(data_type):
            raise ValueError(f'Invalid field type: {data_type}')
    elif isinstance(data_type, DatasetFieldType):
        data_type = data_type.name
    else:
        raise ValueError('data_type must be a str or a DatasetFieldType')

    DatasetType.__members__.get(data_type)
    field = {
        'name': name,
        'type': data_type
    }
    if description:
        field['description'] = description
    return field
