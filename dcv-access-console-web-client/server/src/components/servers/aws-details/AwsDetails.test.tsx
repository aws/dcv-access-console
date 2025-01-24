import {render, screen} from "@testing-library/react";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";
import AwsDetails from "@/components/servers/aws-details/AwsDetails";
import {AwsDetailsEmpty, AwsDetailsNormal} from "@/components/servers/aws-details/AwsDetails.stories";
import {AWS_DETAILS_CONSTANTS} from "@/constants/aws-details-constants";

describe('AwsDetails', () => {
    it('Should render when empty', () => {
        render(<AwsDetails {...AwsDetailsEmpty.args}/>)
        expect(screen.getByText(AWS_DETAILS_CONSTANTS.EMPTY_TEXT, {})).toBeVisible()
    })
    it('Should render with aws', () => {
        render(<AwsDetails {...AwsDetailsNormal.args}/>)
        expect(screen.getByText(SERVERS_TABLE_CONSTANTS.AWS_REGION, {})).toBeVisible();
        expect(screen.getByText(SERVERS_TABLE_CONSTANTS.AWS_EC2_INSTANCE_TYPE, {})).toBeVisible();
        expect(screen.getByText(SERVERS_TABLE_CONSTANTS.AWS_EC2_INSTANCE_ID, {})).toBeVisible();
        expect(screen.getByText(SERVERS_TABLE_CONSTANTS.AWS_EC2_IMAGE_ID, {})).toBeVisible();
    })
})
