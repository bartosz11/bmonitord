export function formatSeconds(seconds) {
  const days = Math.floor(seconds / (24 * 60 * 60));
  seconds %= 24 * 60 * 60;
  const hours = Math.floor(seconds / (60 * 60));
  seconds %= 60 * 60;
  const minutes = Math.floor(seconds / 60);
  seconds %= 60;

  let result = "";
  if (days > 0) {
    result += days + (days === 1 ? " day" : " days") + ", ";
  }
  if (hours > 0) {
    result += hours + (hours === 1 ? " hour" : " hours") + ", ";
  }
  if (minutes > 0) {
    result += minutes + (minutes === 1 ? " minute" : " minutes") + ", ";
  }
  result += seconds + (seconds === 1 ? " second" : " seconds");

  return result;
}
