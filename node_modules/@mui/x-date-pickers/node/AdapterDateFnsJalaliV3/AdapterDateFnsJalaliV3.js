"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.AdapterDateFnsJalali = void 0;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var _addSeconds = require("date-fns-jalali/addSeconds");
var _addMinutes = require("date-fns-jalali/addMinutes");
var _addHours = require("date-fns-jalali/addHours");
var _addDays = require("date-fns-jalali/addDays");
var _addWeeks = require("date-fns-jalali/addWeeks");
var _addMonths = require("date-fns-jalali/addMonths");
var _addYears = require("date-fns-jalali/addYears");
var _endOfDay = require("date-fns-jalali/endOfDay");
var _endOfWeek = require("date-fns-jalali/endOfWeek");
var _endOfYear = require("date-fns-jalali/endOfYear");
var _format = require("date-fns-jalali/format");
var _getHours = require("date-fns-jalali/getHours");
var _getSeconds = require("date-fns-jalali/getSeconds");
var _getMilliseconds = require("date-fns-jalali/getMilliseconds");
var _getWeek = require("date-fns-jalali/getWeek");
var _getYear = require("date-fns-jalali/getYear");
var _getMonth = require("date-fns-jalali/getMonth");
var _getDate = require("date-fns-jalali/getDate");
var _getDaysInMonth = require("date-fns-jalali/getDaysInMonth");
var _getMinutes = require("date-fns-jalali/getMinutes");
var _isAfter = require("date-fns-jalali/isAfter");
var _isBefore = require("date-fns-jalali/isBefore");
var _isEqual = require("date-fns-jalali/isEqual");
var _isSameDay = require("date-fns-jalali/isSameDay");
var _isSameYear = require("date-fns-jalali/isSameYear");
var _isSameMonth = require("date-fns-jalali/isSameMonth");
var _isSameHour = require("date-fns-jalali/isSameHour");
var _isValid = require("date-fns-jalali/isValid");
var _parse = require("date-fns-jalali/parse");
var _setDate = require("date-fns-jalali/setDate");
var _setHours = require("date-fns-jalali/setHours");
var _setMinutes = require("date-fns-jalali/setMinutes");
var _setMonth = require("date-fns-jalali/setMonth");
var _setSeconds = require("date-fns-jalali/setSeconds");
var _setMilliseconds = require("date-fns-jalali/setMilliseconds");
var _setYear = require("date-fns-jalali/setYear");
var _startOfDay = require("date-fns-jalali/startOfDay");
var _startOfMonth = require("date-fns-jalali/startOfMonth");
var _endOfMonth = require("date-fns-jalali/endOfMonth");
var _startOfWeek = require("date-fns-jalali/startOfWeek");
var _startOfYear = require("date-fns-jalali/startOfYear");
var _isWithinInterval = require("date-fns-jalali/isWithinInterval");
var _faIR = require("date-fns-jalali/locale/fa-IR");
var _AdapterDateFnsBase = require("../AdapterDateFnsBase");
/* eslint-disable class-methods-use-this */
// TODO remove when date-fns-jalali-v3 is the default
// @ts-nocheck

// date-fns-jalali v2 does not export types
// @ts-ignore TODO remove when date-fns-jalali-v3 is the default

const defaultFormats = {
  year: 'yyyy',
  month: 'LLLL',
  monthShort: 'MMM',
  dayOfMonth: 'd',
  dayOfMonthFull: 'do',
  weekday: 'EEEE',
  weekdayShort: 'EEEEEE',
  hours24h: 'HH',
  hours12h: 'hh',
  meridiem: 'aa',
  minutes: 'mm',
  seconds: 'ss',
  fullDate: 'PPP',
  keyboardDate: 'P',
  shortDate: 'd MMM',
  normalDate: 'd MMMM',
  normalDateWithWeekday: 'EEE, d MMMM',
  fullTime: 'p',
  fullTime12h: 'hh:mm aaa',
  fullTime24h: 'HH:mm',
  keyboardDateTime: 'P p',
  keyboardDateTime12h: 'P hh:mm aa',
  keyboardDateTime24h: 'P HH:mm'
};
const NUMBER_SYMBOL_MAP = {
  '1': '۱',
  '2': '۲',
  '3': '۳',
  '4': '۴',
  '5': '۵',
  '6': '۶',
  '7': '۷',
  '8': '۸',
  '9': '۹',
  '0': '۰'
};
/**
 * Based on `@date-io/date-fns-jalali`
 *
 * MIT License
 *
 * Copyright (c) 2017 Dmitriy Kovalenko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class AdapterDateFnsJalali extends _AdapterDateFnsBase.AdapterDateFnsBase {
  constructor({
    locale,
    formats
  } = {}) {
    /* istanbul ignore next */
    if (process.env.NODE_ENV !== 'production') {
      if (typeof _addDays.addDays !== 'function') {
        throw new Error([`MUI: The \`date-fns-jalali\` package v2.x is not compatible with this adapter.`, 'Please, install v3.x of the package or use the `AdapterDateFnsJalali` instead.'].join('\n'));
      }
      if (!_format.longFormatters) {
        throw new Error('MUI: The minimum supported `date-fns-jalali` package version compatible with this adapter is `3.2.x`.');
      }
    }
    super({
      locale: locale ?? _faIR.faIR,
      // some formats are different in jalali adapter,
      // this ensures that `AdapterDateFnsBase` formats are overridden
      formats: (0, _extends2.default)({}, defaultFormats, formats),
      longFormatters: _format.longFormatters,
      lib: 'date-fns-jalali'
    });
    // TODO: explicit return types can be removed once there is only one date-fns version supported
    this.parse = (value, format) => {
      if (value === '') {
        return null;
      }
      return (0, _parse.parse)(value, format, new Date(), {
        locale: this.locale
      });
    };
    this.isValid = value => {
      if (value == null) {
        return false;
      }
      return (0, _isValid.isValid)(value);
    };
    this.format = (value, formatKey) => {
      return this.formatByString(value, this.formats[formatKey]);
    };
    this.formatByString = (value, formatString) => {
      return (0, _format.format)(value, formatString, {
        locale: this.locale
      });
    };
    this.formatNumber = numberToFormat => {
      return numberToFormat.replace(/\d/g, match => NUMBER_SYMBOL_MAP[match]).replace(/,/g, '،');
    };
    this.isEqual = (value, comparing) => {
      if (value === null && comparing === null) {
        return true;
      }
      if (value === null || comparing === null) {
        return false;
      }
      return (0, _isEqual.isEqual)(value, comparing);
    };
    this.isSameYear = (value, comparing) => {
      return (0, _isSameYear.isSameYear)(value, comparing);
    };
    this.isSameMonth = (value, comparing) => {
      return (0, _isSameMonth.isSameMonth)(value, comparing);
    };
    this.isSameDay = (value, comparing) => {
      return (0, _isSameDay.isSameDay)(value, comparing);
    };
    this.isSameHour = (value, comparing) => {
      return (0, _isSameHour.isSameHour)(value, comparing);
    };
    this.isAfter = (value, comparing) => {
      return (0, _isAfter.isAfter)(value, comparing);
    };
    this.isAfterYear = (value, comparing) => {
      return (0, _isAfter.isAfter)(value, this.endOfYear(comparing));
    };
    this.isAfterDay = (value, comparing) => {
      return (0, _isAfter.isAfter)(value, this.endOfDay(comparing));
    };
    this.isBefore = (value, comparing) => {
      return (0, _isBefore.isBefore)(value, comparing);
    };
    this.isBeforeYear = (value, comparing) => {
      return (0, _isBefore.isBefore)(value, this.startOfYear(comparing));
    };
    this.isBeforeDay = (value, comparing) => {
      return (0, _isBefore.isBefore)(value, this.startOfDay(comparing));
    };
    this.isWithinRange = (value, [start, end]) => {
      return (0, _isWithinInterval.isWithinInterval)(value, {
        start,
        end
      });
    };
    this.startOfYear = value => {
      return (0, _startOfYear.startOfYear)(value);
    };
    this.startOfMonth = value => {
      return (0, _startOfMonth.startOfMonth)(value);
    };
    this.startOfWeek = value => {
      return (0, _startOfWeek.startOfWeek)(value, {
        locale: this.locale
      });
    };
    this.startOfDay = value => {
      return (0, _startOfDay.startOfDay)(value);
    };
    this.endOfYear = value => {
      return (0, _endOfYear.endOfYear)(value);
    };
    this.endOfMonth = value => {
      return (0, _endOfMonth.endOfMonth)(value);
    };
    this.endOfWeek = value => {
      return (0, _endOfWeek.endOfWeek)(value, {
        locale: this.locale
      });
    };
    this.endOfDay = value => {
      return (0, _endOfDay.endOfDay)(value);
    };
    this.addYears = (value, amount) => {
      return (0, _addYears.addYears)(value, amount);
    };
    this.addMonths = (value, amount) => {
      return (0, _addMonths.addMonths)(value, amount);
    };
    this.addWeeks = (value, amount) => {
      return (0, _addWeeks.addWeeks)(value, amount);
    };
    this.addDays = (value, amount) => {
      return (0, _addDays.addDays)(value, amount);
    };
    this.addHours = (value, amount) => {
      return (0, _addHours.addHours)(value, amount);
    };
    this.addMinutes = (value, amount) => {
      return (0, _addMinutes.addMinutes)(value, amount);
    };
    this.addSeconds = (value, amount) => {
      return (0, _addSeconds.addSeconds)(value, amount);
    };
    this.getYear = value => {
      return (0, _getYear.getYear)(value);
    };
    this.getMonth = value => {
      return (0, _getMonth.getMonth)(value);
    };
    this.getDate = value => {
      return (0, _getDate.getDate)(value);
    };
    this.getHours = value => {
      return (0, _getHours.getHours)(value);
    };
    this.getMinutes = value => {
      return (0, _getMinutes.getMinutes)(value);
    };
    this.getSeconds = value => {
      return (0, _getSeconds.getSeconds)(value);
    };
    this.getMilliseconds = value => {
      return (0, _getMilliseconds.getMilliseconds)(value);
    };
    this.setYear = (value, year) => {
      return (0, _setYear.setYear)(value, year);
    };
    this.setMonth = (value, month) => {
      return (0, _setMonth.setMonth)(value, month);
    };
    this.setDate = (value, date) => {
      return (0, _setDate.setDate)(value, date);
    };
    this.setHours = (value, hours) => {
      return (0, _setHours.setHours)(value, hours);
    };
    this.setMinutes = (value, minutes) => {
      return (0, _setMinutes.setMinutes)(value, minutes);
    };
    this.setSeconds = (value, seconds) => {
      return (0, _setSeconds.setSeconds)(value, seconds);
    };
    this.setMilliseconds = (value, milliseconds) => {
      return (0, _setMilliseconds.setMilliseconds)(value, milliseconds);
    };
    this.getDaysInMonth = value => {
      return (0, _getDaysInMonth.getDaysInMonth)(value);
    };
    this.getWeekArray = value => {
      const start = this.startOfWeek(this.startOfMonth(value));
      const end = this.endOfWeek(this.endOfMonth(value));
      let count = 0;
      let current = start;
      const nestedWeeks = [];
      while (this.isBefore(current, end)) {
        const weekNumber = Math.floor(count / 7);
        nestedWeeks[weekNumber] = nestedWeeks[weekNumber] || [];
        nestedWeeks[weekNumber].push(current);
        current = this.addDays(current, 1);
        count += 1;
      }
      return nestedWeeks;
    };
    this.getWeekNumber = date => {
      return (0, _getWeek.getWeek)(date, {
        locale: this.locale
      });
    };
    this.getYearRange = ([start, end]) => {
      const startDate = this.startOfYear(start);
      const endDate = this.endOfYear(end);
      const years = [];
      let current = startDate;
      while (this.isBefore(current, endDate)) {
        years.push(current);
        current = this.addYears(current, 1);
      }
      return years;
    };
  }
}
exports.AdapterDateFnsJalali = AdapterDateFnsJalali;