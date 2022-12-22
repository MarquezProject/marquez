export default function split(value, delimiter) {
    if (!delimiter) return [value];
    const escaped = delimiter.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    return value.split(new RegExp(`(${escaped})`, 'gi'));
}