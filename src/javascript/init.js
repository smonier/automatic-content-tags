import {registry} from '@jahia/ui-extender';
import register from './AutoTags.register';

export default function () {
    registry.add('callback', 'automatic-content-tags', {
        targets: ['jahiaApp-init:50'],
        callback: register
    });
}
