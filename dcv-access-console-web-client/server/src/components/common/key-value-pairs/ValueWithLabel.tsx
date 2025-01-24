import Box from "@cloudscape-design/components/box";

export const ValueWithLabel = ({ label, children }) => (
    <div>
        <Box variant="awsui-key-label">{label}</Box>
        <div>{children}</div>
    </div>
);