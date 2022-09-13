echo "Test script started...">"test_output"
for (( i = 1; i <= $#; i++ )); do
    sleep 5
    echo "$i: ${@:i:1}">>"test_output"
done
echo "Test script ended!">>"test_output"
